package net.bummy1337.daintegrate.ui;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiTableColumnFlags;
import imgui.flag.ImGuiTableFlags;
import net.bummy1337.dontaionalerts.AlertType;
import net.bummy1337.dontaionalerts.ConnectionState;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

import static net.bummy1337.daintegrate.ui.Theme.col;

class OverviewPage {
    private static final SimpleDateFormat TimeFormat = new SimpleDateFormat("HH:mm:ss");

    private final DaMenu menu;
    private final UiContext context;

    OverviewPage(DaMenu menu, UiContext context) {
        this.menu = menu;
        this.context = context;
    }

    void render() {
        float s = DaMenu.scale();
        Widgets.title(I18n.t("overview.title"), I18n.t("overview.subtitle"));

        var client = context.getClient();
        var state = client.getState();
        var events = context.getEvents();

        if (state != ConnectionState.Connected && state != ConnectionState.Reconnecting && state != ConnectionState.Connecting)
            renderConnectBanner();

        float spacing = ImGui.getStyle().getItemSpacingX();
        float cardWidth = (ImGui.getContentRegionAvailX() - spacing * 3) / 4;
        statCard("##stat-status", cardWidth, I18n.t("overview.status"), I18n.t("state." + state.name()), Widgets.stateColor(state));
        ImGui.sameLine();
        statCard("##stat-triggers", cardWidth, I18n.t("overview.triggers"), String.valueOf(activeTriggers()), Theme.Accent);
        ImGui.sameLine();
        statCard("##stat-events", cardWidth, I18n.t("overview.events"), String.valueOf(events.size()), Theme.Info);
        ImGui.sameLine();
        var last = events.isEmpty() ? null : events.get(events.size() - 1);
        statCard("##stat-last", cardWidth, I18n.t("overview.last"),
                last == null ? I18n.t("never") : formatAmount(last), Theme.Purple);

        Widgets.gap(10);
        Widgets.caption(I18n.t("overview.recent"));
        Widgets.gap(2);

        if (events.isEmpty()) {
            if (Widgets.beginCard("##empty", 0, 0)) {
                Widgets.gap(8);
                Widgets.bold(I18n.t("overview.empty"));
                Widgets.muted(I18n.t("overview.empty.hint"));
                Widgets.gap(8);
            }
            Widgets.endCard();
            return;
        }

        int flags = ImGuiTableFlags.RowBg | ImGuiTableFlags.BordersInnerH | ImGuiTableFlags.ScrollY | ImGuiTableFlags.SizingStretchProp;
        if (ImGui.beginTable("##events", 5, flags, 0, ImGui.getContentRegionAvailY())) {
            ImGui.tableSetupScrollFreeze(0, 1);
            ImGui.tableSetupColumn(I18n.t("col.time").toLowerCase(), ImGuiTableColumnFlags.WidthFixed, 80 * s);
            ImGui.tableSetupColumn(I18n.t("col.type").toLowerCase(), ImGuiTableColumnFlags.WidthFixed, 150 * s);
            ImGui.tableSetupColumn(I18n.t("col.from").toLowerCase(), ImGuiTableColumnFlags.WidthStretch, 1f);
            ImGui.tableSetupColumn(I18n.t("col.amount").toLowerCase(), ImGuiTableColumnFlags.WidthFixed, 110 * s);
            ImGui.tableSetupColumn(I18n.t("col.message").toLowerCase(), ImGuiTableColumnFlags.WidthStretch, 2f);
            ImGui.pushStyleColor(ImGuiCol.Text, Theme.white(Theme.TextMutedAlpha));
            ImGui.tableHeadersRow();
            ImGui.popStyleColor();
            for (int i = events.size() - 1; i >= 0; i--) {
                var event = events.get(i);
                ImGui.tableNextRow(0, 30 * s);
                ImGui.tableSetColumnIndex(0);
                ImGui.alignTextToFramePadding();
                ImGui.textDisabled(TimeFormat.format(new Date(event.getReceivedAt())));
                ImGui.tableSetColumnIndex(1);
                Widgets.badge(typeName(event.getType()), typeColor(event.getType()));
                if (event.isTest()) {
                    ImGui.sameLine();
                    Widgets.badge(I18n.t("test"), Theme.TextMuted);
                }
                ImGui.tableSetColumnIndex(2);
                ImGui.text(event.getUserName() == null ? "—" : event.getUserName());
                ImGui.tableSetColumnIndex(3);
                ImGui.pushStyleColor(ImGuiCol.Text, col(Theme.Success));
                ImGui.text(formatAmount(event));
                ImGui.popStyleColor();
                ImGui.tableSetColumnIndex(4);
                ImGui.textWrapped(event.getMessage() == null ? "" : event.getMessage());
            }
            ImGui.endTable();
        }
    }

    private void renderConnectBanner() {
        float s = DaMenu.scale();
        ImGui.pushStyleColor(ImGuiCol.ChildBg, col(Theme.Accent, 0.09f));
        ImGui.pushStyleColor(ImGuiCol.Border, col(Theme.Accent, 0.35f));
        if (Widgets.beginCard("##banner", 0, 0)) {
            float buttonWidth = 240 * s;
            ImGui.beginGroup();
            Widgets.bold(I18n.t("overview.connect.cta"));
            Widgets.muted(I18n.t("overview.connect.hint"));
            ImGui.endGroup();
            ImGui.sameLine(ImGui.getContentRegionMaxX() - buttonWidth);
            ImGui.setCursorPosY(ImGui.getCursorPosY() + 6 * s);
            if (Widgets.primaryButton(I18n.t("overview.connect.cta"), buttonWidth))
                menu.open(DaMenu.Page.Connection);
        }
        Widgets.endCard();
        ImGui.popStyleColor(2);
        Widgets.gap(4);
    }

    private void statCard(String id, float width, String label, String value, int accent) {
        float s = DaMenu.scale();
        if (Widgets.beginCard(id, width, 92 * s)) {
            Widgets.statusDot(accent, false);
            Widgets.caption(label);
            Widgets.gap(2);
            // Numbers use the big title font; long words (like a status) drop to the heading font
            var big = DaMenu.fonts().title();
            ImGui.pushFont(big, big.getLegacySize());
            boolean fits = ImGui.calcTextSizeX(value) <= ImGui.getContentRegionAvailX();
            ImGui.popFont();
            if (fits) {
                Widgets.font(big, () -> ImGui.text(value));
            } else {
                Widgets.gap(6);
                ImGui.pushStyleColor(ImGuiCol.Text, col(accent));
                Widgets.font(DaMenu.fonts().heading(), () -> ImGui.text(value));
                ImGui.popStyleColor();
            }
        }
        Widgets.endCard();
    }

    private int activeTriggers() {
        var settings = context.getSettings();
        if (settings == null || settings.triggers == null)
            return 0;
        int count = 0;
        for (var trigger : settings.triggers)
            if (trigger != null && trigger.isActive)
                count++;
        return count;
    }

    static String formatAmount(ReadOnlyDonationAlertsEvent event) {
        float amount = event.getAmount();
        if (amount == 0 && (event.getCurrency() == null || event.getCurrency().isBlank()))
            return "—";
        var text = amount == Math.floor(amount) ? String.valueOf((long) amount) : String.format("%.2f", amount);
        var currency = event.getCurrency();
        return currency == null || currency.isBlank() ? text : text + " " + currency;
    }

    static String typeName(AlertType type) {
        if (type == null)
            return "?";
        return switch (type) {
            case Donate -> I18n.t("kind.Donate");
            case TwitchBits -> I18n.t("kind.TwitchBits");
            case TwitchPoints -> I18n.t("kind.TwitchPoints");
            case Undefined -> "?";
            default -> I18n.t("sub." + type.name());
        };
    }

    static int typeColor(AlertType type) {
        if (type == null)
            return Theme.TextMuted;
        return switch (type) {
            case Donate -> Theme.Accent;
            case TwitchBits, TwitchPoints, TwitchSubscription, TwitchPrimeSubscription, TwitchGiftSubscription, TwitchFreeFollow -> Theme.Purple;
            case YouTubeSubscription -> Theme.Danger;
            default -> Theme.TextMuted;
        };
    }
}
