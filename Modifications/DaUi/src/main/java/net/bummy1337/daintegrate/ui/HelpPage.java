package net.bummy1337.daintegrate.ui;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import net.bummy1337.daintegrate.Constants;

import static net.bummy1337.daintegrate.ui.Theme.col;
import static net.bummy1337.daintegrate.ui.Theme.white;

class HelpPage {
    private final DaMenu menu;
    private final UiContext context;

    HelpPage(DaMenu menu, UiContext context) {
        this.menu = menu;
        this.context = context;
    }

    void render() {
        float s = DaMenu.scale();
        Widgets.title(I18n.t("help.title"), null);
        float spacing = ImGui.getStyle().getItemSpacingX();
        float available = Math.min(ImGui.getContentRegionAvailX(), 900 * s);
        float column = (available - spacing) / 2;

        ImGui.beginGroup();
        if (Widgets.beginGroup("##placeholders", I18n.t("help.placeholders"), column)) {
            row("{username}", I18n.t("help.ph.username"));
            row("{amount}", I18n.t("help.ph.amount"));
            row("{currency}", I18n.t("help.ph.currency"));
            row("{message}", I18n.t("help.ph.message"));
            row("{playername}", I18n.t("help.ph.playername"));
        }
        Widgets.endCard();
        ImGui.endGroup();
        ImGui.sameLine();

        ImGui.beginGroup();
        if (Widgets.beginGroup("##keys", I18n.t("help.keys"), column)) {
            keyRow(new String[]{context.getOpenKeyName()}, I18n.t("help.keys.open.short"));
            keyRow(new String[]{"Ctrl", "S"}, I18n.t("help.keys.save.short"));
            keyRow(new String[]{"Esc"}, I18n.t("help.keys.close.short"));
        }
        Widgets.endCard();

        if (Widgets.beginGroup("##commands", I18n.t("help.commands"), column)) {
            command("/da set", I18n.t("help.cmd.set.short"));
            command("/da connect", I18n.t("help.cmd.connect.short"));
            command("/da disconnect", I18n.t("help.cmd.disconnect.short"));
            command("/da status", I18n.t("help.cmd.status.short"));
        }
        Widgets.endCard();
        ImGui.endGroup();

        Widgets.gap(6);
        if (Widgets.link(I18n.t("help.guide")))
            context.openUrl(Constants.ModUrl);
    }

    private static void row(String placeholder, String description) {
        float s = DaMenu.scale();
        ImGui.pushStyleColor(ImGuiCol.Text, col(Theme.AccentLight));
        ImGui.text(placeholder);
        ImGui.popStyleColor();
        ImGui.sameLine(130 * s);
        ImGui.pushStyleColor(ImGuiCol.Text, white(Theme.TextSecondaryAlpha));
        ImGui.text(description);
        ImGui.popStyleColor();
    }

    private static void keyRow(String[] keys, String description) {
        float s = DaMenu.scale();
        float y = ImGui.getCursorPosY();
        for (int i = 0; i < keys.length; i++) {
            if (i > 0) {
                ImGui.sameLine(0, 4 * s);
                ImGui.textDisabled("+");
                ImGui.sameLine(0, 4 * s);
            }
            Widgets.keycap(keys[i]);
        }
        ImGui.sameLine(130 * s);
        ImGui.setCursorPosY(y + 2 * s);
        ImGui.pushStyleColor(ImGuiCol.Text, white(Theme.TextSecondaryAlpha));
        ImGui.text(description);
        ImGui.popStyleColor();
    }

    private static void command(String command, String description) {
        float s = DaMenu.scale();
        ImGui.text(command);
        ImGui.sameLine(130 * s);
        ImGui.pushStyleColor(ImGuiCol.Text, white(Theme.TextSecondaryAlpha));
        ImGui.text(description);
        ImGui.popStyleColor();
    }
}
