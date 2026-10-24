package net.bummy1337.daintegrate.ui;

import imgui.ImGui;
import imgui.flag.ImGuiChildFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiMouseCursor;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import net.bummy1337.daintegrate.configurations.SettingsDto;
import net.bummy1337.daintegrate.configurations.TriggerDto;
import net.bummy1337.dontaionalerts.AlertType;
import net.bummy1337.dontaionalerts.DonationAlertsEvent;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;

import java.util.ArrayList;
import java.util.List;

import static net.bummy1337.daintegrate.ui.Theme.col;
import static net.bummy1337.daintegrate.ui.Theme.white;

/**
 * List of triggers on the left, editor of the selected one on the right.
 * Edits stay in memory until saved, so a half-typed trigger never fires on stream.
 */
class TriggersPage {
    private static final String[] Placeholders = { "{username}", "{amount}", "{currency}", "{message}", "{playername}" };

    private final DaMenu menu;
    private final UiContext context;

    private final List<TriggerModel> working = new ArrayList<>();
    private final List<TriggerModel> baseline = new ArrayList<>();
    private SettingsDto loadedFrom;
    private int selected = -1;
    private String search = "";
    private int focusedAction = -1;
    private int pendingDelete = -1;
    private String toast;
    private boolean toastIsError;
    private double toastUntil;

    TriggersPage(DaMenu menu, UiContext context) {
        this.menu = menu;
        this.context = context;
    }

    boolean isDirty() {
        if (working.size() != baseline.size())
            return true;
        for (int i = 0; i < working.size(); i++)
            if (!working.get(i).sameAs(baseline.get(i)))
                return true;
        return false;
    }

    void save() {
        var settings = context.getSettings();
        if (settings == null)
            settings = new SettingsDto();
        var triggers = new TriggerDto[working.size()];
        for (int i = 0; i < working.size(); i++)
            triggers[i] = working.get(i).toDto();
        settings.triggers = triggers;
        try {
            context.saveSettings(settings);
            loadedFrom = context.getSettings();
            baseline.clear();
            for (var model : working)
                baseline.add(model.copy());
            showToast(I18n.t("trg.saved"), false);
        } catch (Exception e) {
            showToast(I18n.t("trg.saveError", e.getMessage()), true);
        }
    }

    void render() {
        syncWithSettings();
        float s = DaMenu.scale();

        renderHeader();

        float listWidth = 270 * s;
        float height = ImGui.getContentRegionAvailY();
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 12 * s, 12 * s);
        ImGui.beginChild("##trigger-list", listWidth, height, ImGuiChildFlags.Borders | ImGuiChildFlags.AlwaysUseWindowPadding, 0);
        ImGui.popStyleVar();
        renderList();
        ImGui.endChild();

        ImGui.sameLine();
        ImGui.pushStyleColor(ImGuiCol.ChildBg, col(Theme.Black, 0f));
        ImGui.beginChild("##trigger-editor", 0, height, ImGuiChildFlags.None, 0);
        if (selected >= 0 && selected < working.size())
            renderEditor(working.get(selected));
        else
            renderEmptyEditor();
        ImGui.endChild();
        ImGui.popStyleColor();

        renderDeleteModal();
    }

    private void syncWithSettings() {
        var settings = context.getSettings();
        if (settings == loadedFrom)
            return;
        // Reload when the file changed on disk, unless the user has unsaved edits
        if (loadedFrom != null && isDirty())
            return;
        loadedFrom = settings;
        working.clear();
        baseline.clear();
        if (settings != null && settings.triggers != null) {
            for (var dto : settings.triggers) {
                if (dto == null)
                    continue;
                var model = TriggerModel.fromDto(dto);
                working.add(model);
                baseline.add(model.copy());
            }
        }
        if (selected >= working.size())
            selected = working.size() - 1;
        if (selected < 0 && !working.isEmpty())
            selected = 0;
    }

    private void renderHeader() {
        float s = DaMenu.scale();
        float buttonsWidth = 300 * s;
        ImGui.beginGroup();
        Widgets.title(I18n.t("trg.title"), I18n.t("trg.subtitle"));
        ImGui.endGroup();

        boolean dirty = isDirty();
        boolean toastVisible = toast != null && ImGui.getTime() < toastUntil;
        if (!dirty && !toastVisible)
            return;

        var after = ImGui.getCursorPos();
        ImGui.setCursorPos(ImGui.getWindowWidth() - buttonsWidth - 70 * s, 18 * s);
        ImGui.beginGroup();
        if (dirty) {
            Widgets.statusDot(Theme.Accent, false, true);
            ImGui.alignTextToFramePadding();
            ImGui.pushStyleColor(ImGuiCol.Text, col(Theme.TextSecondary));
            ImGui.text(I18n.t("trg.unsaved"));
            ImGui.popStyleColor();
            ImGui.sameLine();
            if (Widgets.button(I18n.t("trg.revert"), 0)) {
                loadedFrom = null;
                baseline.clear();
                working.clear();
                syncWithSettings();
            }
            ImGui.sameLine();
            if (Widgets.primaryButton(I18n.t("trg.save"), 0))
                save();
            Widgets.tooltip("Ctrl+S");
        } else {
            Widgets.statusDot(toastIsError ? Theme.Danger : Theme.Success, false, true);
            ImGui.alignTextToFramePadding();
            ImGui.pushStyleColor(ImGuiCol.Text, col(toastIsError ? Theme.Danger : Theme.Success));
            ImGui.text(toast);
            ImGui.popStyleColor();
        }
        if (dirty && toastVisible && toastIsError)
            Widgets.colored(Theme.Danger, toast);
        ImGui.endGroup();
        ImGui.setCursorPos(after.x, after.y);
    }

    private void renderList() {
        float s = DaMenu.scale();
        float width = ImGui.getContentRegionAvailX();
        search = Widgets.input("##search", I18n.t("trg.search"), search, width, 128);
        if (Widgets.primaryButton("+  " + I18n.t("trg.add"), width))
            addTrigger(TriggerModel.create(I18n.t("trg.newName")));
        Widgets.gap(4);

        if (working.isEmpty()) {
            Widgets.gap(16);
            Widgets.bold(I18n.t("trg.empty"));
            Widgets.muted(I18n.t("trg.empty.hint"));
            return;
        }

        var draw = ImGui.getWindowDrawList();
        for (int i = 0; i < working.size(); i++) {
            var model = working.get(i);
            if (!model.matches(search))
                continue;
            boolean isSelected = i == selected;
            var pos = ImGui.getCursorScreenPos();
            float rowHeight = 54 * s;

            ImGui.pushID(i);
            if (ImGui.invisibleButton("##row", width, rowHeight)) {
                selected = i;
                focusedAction = -1;
            }
            boolean hovered = ImGui.isItemHovered();
            ImGui.popID();
            if (hovered)
                ImGui.setMouseCursor(ImGuiMouseCursor.Hand);

            if (isSelected)
                draw.addRectFilled(pos.x, pos.y, pos.x + width, pos.y + rowHeight, white(0.09f), 10 * s);
            else if (hovered)
                draw.addRectFilled(pos.x, pos.y, pos.x + width, pos.y + rowHeight, white(0.045f), 10 * s);

            int kindColor = kindColor(model.kind);
            draw.addRectFilled(pos.x + 8 * s, pos.y + 12 * s, pos.x + 11 * s, pos.y + rowHeight - 12 * s, col(kindColor, model.active ? 1f : 0.35f), 2 * s);

            float textX = pos.x + 22 * s;
            var name = model.name == null || model.name.isBlank() ? "—" : model.name;
            ImGui.pushFont(DaMenu.fonts().bold(), DaMenu.fonts().bold().getLegacySize());
            draw.addText(textX, pos.y + 9 * s, col(model.active ? Theme.Text : Theme.TextMuted), clip(name, width - 50 * s));
            ImGui.popFont();
            draw.addText(textX, pos.y + 29 * s, col(Theme.TextMuted), clip(summary(model), width - 50 * s));

            float dotX = pos.x + width - 16 * s;
            draw.addCircleFilled(dotX, pos.y + rowHeight / 2, 4 * s, model.active ? col(Theme.Success) : white(0.2f));
            if (i < baseline.size() && !model.sameAs(baseline.get(i)) || i >= baseline.size())
                draw.addCircleFilled(dotX - 12 * s, pos.y + rowHeight / 2, 3 * s, col(Theme.Accent));
        }
    }

    private void renderEmptyEditor() {
        float s = DaMenu.scale();
        ImGui.setCursorPos(24 * s, 40 * s);
        Widgets.muted(I18n.t("trg.select"));
    }

    private void renderEditor(TriggerModel model) {
        float s = DaMenu.scale();
        ImGui.pushID("editor" + selected);

        // Name and enabled toggle
        var enabledLabel = I18n.t("trg.enabled").toLowerCase();
        float toggleWidth = 38 * s + ImGui.calcTextSizeX(enabledLabel) + 30 * s;
        ImGui.pushFont(DaMenu.fonts().heading(), DaMenu.fonts().heading().getLegacySize());
        model.name = Widgets.input("##name", I18n.t("trg.name"), model.name, ImGui.getContentRegionAvailX() - toggleWidth, 128);
        float nameHeight = ImGui.getFrameHeight();
        ImGui.popFont();
        ImGui.sameLine(0, 16 * s);
        float y = ImGui.getCursorPosY() + (nameHeight - ImGui.getFrameHeight()) / 2;
        ImGui.setCursorPosY(y);
        ImGui.alignTextToFramePadding();
        ImGui.pushStyleColor(ImGuiCol.Text, white(Theme.TextSecondaryAlpha));
        ImGui.text(enabledLabel);
        ImGui.popStyleColor();
        ImGui.sameLine();
        ImGui.setCursorPosY(y);
        if (Widgets.toggle("##enabled", model.active))
            model.active = !model.active;

        Widgets.gap(4);
        renderCondition(model);
        renderActions(model);
        renderFooter(model);

        ImGui.popID();
    }

    private void renderCondition(TriggerModel model) {
        float s = DaMenu.scale();
        if (Widgets.beginGroup("##when", I18n.t("trg.when"), 0)) {
            var kinds = TriggerModel.Kind.values();
            var labels = new String[kinds.length];
            for (int i = 0; i < kinds.length; i++)
                labels[i] = I18n.t("seg." + kinds[i].name());
            int picked = Widgets.segmented("##kind", labels, model.kind.ordinal(), ImGui.getContentRegionAvailX());
            model.kind = kinds[picked];
            Widgets.tooltip(I18n.t("kind." + model.kind.name()));
            Widgets.gap(2);

            switch (model.kind) {
                case Donate, TwitchBits, TwitchPoints -> {
                    ImGui.alignTextToFramePadding();
                    ImGui.text(I18n.t("trg.from"));
                    ImGui.sameLine(110 * s);
                    model.from = number("##from", model.from, false, 120 * s);
                    ImGui.sameLine(0, 14 * s);
                    ImGui.text(I18n.t("trg.to"));
                    ImGui.sameLine();
                    model.to = number("##to", model.to, true, 120 * s);
                    Widgets.tooltip(I18n.t("trg.to.hint"));
                    if (model.kind == TriggerModel.Kind.Donate) {
                        ImGui.alignTextToFramePadding();
                        ImGui.text(I18n.t("trg.currency"));
                        ImGui.sameLine(110 * s);
                        ImGui.setNextItemWidth(120 * s);
                        var preview = model.currency == null || model.currency.isBlank() ? I18n.t("trg.currency.any") : model.currency;
                        if (ImGui.beginCombo("##currency", preview)) {
                            for (var currency : TriggerModel.Currencies) {
                                var label = currency.isEmpty() ? I18n.t("trg.currency.any") : currency;
                                if (ImGui.selectable(label, currency.equalsIgnoreCase(model.currency == null ? "" : model.currency)))
                                    model.currency = currency;
                            }
                            ImGui.endCombo();
                        }
                    }
                }
                case Subscribe -> {
                    ImGui.alignTextToFramePadding();
                    ImGui.text(I18n.t("trg.sub.type"));
                    ImGui.sameLine(110 * s);
                    ImGui.setNextItemWidth(260 * s);
                    if (ImGui.beginCombo("##subtype", I18n.t("sub." + model.subscriptionType))) {
                        for (var type : TriggerModel.SubscriptionTypes) {
                            if (ImGui.selectable(I18n.t("sub." + type), type.equals(model.subscriptionType)))
                                model.subscriptionType = type;
                        }
                        ImGui.endCombo();
                    }
                }
                default -> {
                }
            }
        }
        Widgets.endCard();
    }

    private void renderActions(TriggerModel model) {
        float s = DaMenu.scale();
        if (Widgets.beginGroup("##then", I18n.t("trg.then"), 0)) {

            if (model.actions.isEmpty())
                Widgets.muted(I18n.t("trg.action.none"));

            int moveFrom = -1;
            int moveTo = -1;
            int remove = -1;
            for (int i = 0; i < model.actions.size(); i++) {
                var action = model.actions.get(i);
                ImGui.pushID(i);

                // Each action is a small block: type and controls on top, the text below
                var blockStart = ImGui.getCursorScreenPos();
                float blockWidth = ImGui.getContentRegionAvailX();
                var draw = ImGui.getWindowDrawList();
                draw.channelsSplit(2);
                draw.channelsSetCurrent(1);
                ImGui.indent(10 * s);
                ImGui.dummy(0, 2 * s);

                float frame = ImGui.getFrameHeight();
                float spacing = ImGui.getStyle().getItemSpacingX();
                float delayWidth = 56 * s;
                var seconds = I18n.t("trg.action.seconds");
                float controls = delayWidth + spacing + ImGui.calcTextSizeX(seconds) + 3 * (frame + spacing) + 10 * s;

                var typeLine = ImGui.getCursorPosY();
                var types = new String[]{I18n.t("seg.message"), I18n.t("seg.command")};
                int type = Widgets.segmented("##type", types, action.type.ordinal(), Math.min(230 * s, blockWidth - controls - 30 * s));
                action.type = TriggerModel.ActionType.values()[type];

                // The segmented control is a little taller than a frame; centre the controls on it
                ImGui.sameLine(blockWidth - controls);
                ImGui.setCursorPosY(typeLine + 3 * s);
                action.delayTicks = Math.round(number("##delay", action.delayTicks / 20f, false, delayWidth) * 20);
                Widgets.tooltip(I18n.t("trg.action.delay"));
                ImGui.sameLine(0, 4 * s);
                ImGui.alignTextToFramePadding();
                ImGui.textDisabled(seconds);
                ImGui.sameLine();
                if (iconButton("##up", Icons.Kind.Up, i > 0)) {
                    moveFrom = i;
                    moveTo = i - 1;
                }
                ImGui.sameLine();
                if (iconButton("##down", Icons.Kind.Down, i < model.actions.size() - 1)) {
                    moveFrom = i;
                    moveTo = i + 1;
                }
                ImGui.sameLine();
                if (iconButton("##remove", Icons.Kind.Trash, true))
                    remove = i;

                var hint = action.type == TriggerModel.ActionType.Command
                        ? "give {playername} diamond 1"
                        : I18n.t("trg.action.hint");
                action.text = Widgets.input("##text", hint, action.text, blockWidth - 20 * s, 1024);
                if (ImGui.isItemActive())
                    focusedAction = i;
                ImGui.dummy(0, 2 * s);
                ImGui.unindent(10 * s);

                draw.channelsSetCurrent(0);
                var blockEnd = ImGui.getCursorScreenPos();
                boolean focused = focusedAction == i;
                draw.addRectFilled(blockStart.x, blockStart.y, blockStart.x + blockWidth, blockEnd.y - ImGui.getStyle().getItemSpacingY() + 4 * s,
                        white(0.035f), 12 * s);
                if (focused)
                    draw.addRect(blockStart.x, blockStart.y, blockStart.x + blockWidth, blockEnd.y - ImGui.getStyle().getItemSpacingY() + 4 * s,
                            col(Theme.Accent, 0.45f), 12 * s);
                draw.channelsMerge();
                ImGui.dummy(0, 2 * s);

                ImGui.popID();
            }
            if (moveFrom >= 0) {
                var action = model.actions.remove(moveFrom);
                model.actions.add(moveTo, action);
                focusedAction = moveTo;
            }
            if (remove >= 0) {
                model.actions.remove(remove);
                focusedAction = -1;
            }

            if (Widgets.button(I18n.t("trg.action.add.message"), 0)) {
                model.actions.add(new TriggerModel.Action(TriggerModel.ActionType.Message, "", 0));
                focusedAction = model.actions.size() - 1;
            }
            ImGui.sameLine();
            if (Widgets.button(I18n.t("trg.action.add.command"), 0)) {
                model.actions.add(new TriggerModel.Action(TriggerModel.ActionType.Command, "", 0));
                focusedAction = model.actions.size() - 1;
            }

            Widgets.gap(2);
            ImGui.pushStyleColor(ImGuiCol.Text, col(Theme.TextMuted));
            ImGui.text(I18n.t("trg.placeholders"));
            ImGui.popStyleColor();
            float right = ImGui.getCursorScreenPosX() + ImGui.getContentRegionAvailX();
            for (var placeholder : Placeholders) {
                ImGui.sameLine();
                float chipWidth = ImGui.calcTextSizeX(placeholder) + 16 * s;
                if (ImGui.getCursorScreenPosX() + chipWidth > right)
                    ImGui.newLine();
                if (chip(placeholder)) {
                    int target = focusedAction >= 0 && focusedAction < model.actions.size() ? focusedAction : model.actions.size() - 1;
                    if (target >= 0) {
                        var action = model.actions.get(target);
                        action.text = (action.text == null ? "" : action.text) + placeholder;
                    }
                }
            }
        }
        Widgets.endCard();
    }

    private void renderFooter(TriggerModel model) {
        float s = DaMenu.scale();
        Widgets.gap(2);
        if (Widgets.primaryButton(I18n.t("trg.test"), 0))
            test(model);
        Widgets.tooltip(I18n.t("trg.test.tooltip"));
        ImGui.sameLine();
        if (Widgets.button(I18n.t("trg.duplicate"), 0)) {
            var copy = model.copy();
            copy.name = model.name + I18n.t("trg.copySuffix");
            addTrigger(copy);
        }
        ImGui.sameLine();
        if (Widgets.dangerButton(I18n.t("trg.delete"), 0)) {
            pendingDelete = selected;
            ImGui.openPopup("##delete");
        }
    }

    private void renderDeleteModal() {
        float s = DaMenu.scale();
        if (!ImGui.beginPopupModal("##delete", ImGuiWindowFlags.AlwaysAutoResize | ImGuiWindowFlags.NoTitleBar))
            return;
        var name = pendingDelete >= 0 && pendingDelete < working.size() ? working.get(pendingDelete).name : "";
        Widgets.heading(I18n.t("trg.delete.confirm", name));
        Widgets.gap(8);
        if (Widgets.dangerButton(I18n.t("trg.delete"), 140 * s)) {
            if (pendingDelete >= 0 && pendingDelete < working.size()) {
                working.remove(pendingDelete);
                selected = Math.min(pendingDelete, working.size() - 1);
            }
            pendingDelete = -1;
            ImGui.closeCurrentPopup();
        }
        ImGui.sameLine();
        if (Widgets.button(I18n.t("cancel"), 140 * s) || ImGui.isKeyPressed(imgui.flag.ImGuiKey.Escape)) {
            pendingDelete = -1;
            ImGui.closeCurrentPopup();
        }
        ImGui.endPopup();
    }

    private void addTrigger(TriggerModel model) {
        working.add(model);
        selected = working.size() - 1;
        focusedAction = model.actions.isEmpty() ? -1 : 0;
        search = "";
    }

    private void test(TriggerModel model) {
        var event = new DonationAlertsEvent();
        event.ID = -1;
        event.UserName = I18n.isRussian() ? "Тестовый зритель" : "Test viewer";
        event.Message = I18n.isRussian() ? "Это проверка триггера" : "This is a trigger test";
        event.Amount = model.to >= TriggerModel.Unlimited ? Math.max(model.from, 100) : (model.from + model.to) / 2;
        event.Currency = model.currency == null || model.currency.isBlank() ? "RUB" : model.currency;
        event.Type = switch (model.kind) {
            case Subscribe -> AlertType.valueOf(model.subscriptionType);
            case TwitchBits -> AlertType.TwitchBits;
            case TwitchPoints -> AlertType.TwitchPoints;
            default -> AlertType.Donate;
        };
        // Run only this trigger's actions, even if it is not saved yet
        context.simulate(new ReadOnlyDonationAlertsEvent(event), model.toDto());
        showToast(I18n.t("trg.test.sent"), false);
    }

    private void showToast(String text, boolean error) {
        toast = text;
        toastIsError = error;
        toastUntil = ImGui.getTime() + 3;
    }

    private static boolean chip(String text) {
        float s = DaMenu.scale();
        var draw = ImGui.getWindowDrawList();
        var pos = ImGui.getCursorScreenPos();
        float w = ImGui.calcTextSizeX(text) + 16 * s;
        float h = ImGui.getTextLineHeight() + 6 * s;
        boolean clicked = ImGui.invisibleButton(text, w, h);
        boolean hovered = ImGui.isItemHovered();
        if (hovered)
            ImGui.setMouseCursor(ImGuiMouseCursor.Hand);
        draw.addRectFilled(pos.x, pos.y, pos.x + w, pos.y + h, col(Theme.Accent, hovered ? 0.25f : 0.12f), h / 2);
        draw.addText(pos.x + 8 * s, pos.y + 3 * s, col(Theme.AccentLight), text);
        return clicked;
    }

    private static boolean iconButton(String id, Icons.Kind icon, boolean enabled) {
        float size = ImGui.getFrameHeight();
        var draw = ImGui.getWindowDrawList();
        var pos = ImGui.getCursorScreenPos();
        ImGui.beginDisabled(!enabled);
        boolean clicked = ImGui.invisibleButton(id, size, size);
        boolean hovered = ImGui.isItemHovered();
        ImGui.endDisabled();
        int color = !enabled ? white(0.18f) : icon == Icons.Kind.Trash && hovered ? col(Theme.Danger) : white(hovered ? Theme.TextAlpha : Theme.TextSecondaryAlpha);
        draw.addCircleFilled(pos.x + size / 2, pos.y + size / 2, size / 2, white(hovered && enabled ? 0.10f : 0.05f));
        Icons.draw(draw, icon, pos.x + size / 2, pos.y + size / 2, size * 0.38f, color);
        if (hovered && enabled)
            ImGui.setMouseCursor(ImGuiMouseCursor.Hand);
        return clicked && enabled;
    }

    /** Numeric field; with {@code unlimited} an empty value means "no upper limit". */
    private static float number(String id, float value, boolean unlimited, float width) {
        String text;
        if (unlimited && value >= TriggerModel.Unlimited)
            text = "";
        else
            text = value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
        var result = Widgets.input(id, unlimited ? "∞" : "0", text, width, 32, ImGuiInputTextFlags.CharsDecimal);
        if (result.equals(text))
            return value;
        if (result.isBlank())
            return unlimited ? TriggerModel.Unlimited : 0;
        try {
            return Math.max(0, Float.parseFloat(result.replace(',', '.')));
        } catch (NumberFormatException e) {
            return value;
        }
    }

    private static String summary(TriggerModel model) {
        var kind = I18n.t("kind." + model.kind.name());
        return switch (model.kind) {
            case Donate, TwitchBits, TwitchPoints -> {
                var range = format(model.from) + (model.to >= TriggerModel.Unlimited ? "+" : "–" + format(model.to));
                var currency = model.kind == TriggerModel.Kind.Donate && model.currency != null && !model.currency.isBlank() ? " " + model.currency : "";
                yield kind + " · " + range + currency;
            }
            case Subscribe -> I18n.t("sub." + model.subscriptionType);
            default -> kind;
        };
    }

    private static String format(float value) {
        return value == Math.floor(value) ? String.valueOf((long) value) : String.valueOf(value);
    }

    private static int kindColor(TriggerModel.Kind kind) {
        return switch (kind) {
            case Donate -> Theme.Accent;
            case Subscribe -> Theme.Danger;
            case TwitchBits, TwitchPoints -> Theme.Purple;
            default -> Theme.Info;
        };
    }

    private static String clip(String text, float width) {
        if (ImGui.calcTextSizeX(text) <= width)
            return text;
        var ellipsis = "…";
        int end = text.length();
        while (end > 0 && ImGui.calcTextSizeX(text.substring(0, end) + ellipsis) > width)
            end--;
        return text.substring(0, end) + ellipsis;
    }
}
