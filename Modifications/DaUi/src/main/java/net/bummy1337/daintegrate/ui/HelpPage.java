package net.bummy1337.daintegrate.ui;

import imgui.ImGui;
import imgui.flag.ImGuiCol;
import net.bummy1337.daintegrate.Constants;

import static net.bummy1337.daintegrate.ui.Theme.col;

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
        float width = Math.min(ImGui.getContentRegionAvailX(), 720 * s);

        if (Widgets.beginCard("##placeholders", width, 0)) {
            Widgets.heading(I18n.t("help.placeholders"));
            row("{username}", I18n.t("help.ph.username"));
            row("{amount}", I18n.t("help.ph.amount"));
            row("{currency}", I18n.t("help.ph.currency"));
            row("{message}", I18n.t("help.ph.message"));
            row("{playername}", I18n.t("help.ph.playername"));
        }
        Widgets.endCard();

        if (Widgets.beginCard("##keys", width, 0)) {
            Widgets.heading(I18n.t("help.keys"));
            ImGui.text(I18n.t("help.keys.open", context.getOpenKeyName()));
            ImGui.text(I18n.t("help.keys.save"));
        }
        Widgets.endCard();

        if (Widgets.beginCard("##commands", width, 0)) {
            Widgets.heading(I18n.t("help.commands"));
            ImGui.text(I18n.t("help.cmd.set"));
            ImGui.text(I18n.t("help.cmd.connect"));
            ImGui.text(I18n.t("help.cmd.disconnect"));
            ImGui.text(I18n.t("help.cmd.status"));
        }
        Widgets.endCard();

        Widgets.gap(4);
        if (Widgets.link(I18n.t("help.guide")))
            context.openUrl(Constants.ModUrl);
    }

    private static void row(String placeholder, String description) {
        float s = DaMenu.scale();
        ImGui.pushStyleColor(ImGuiCol.Text, col(Theme.AccentHover));
        ImGui.text(placeholder);
        ImGui.popStyleColor();
        ImGui.sameLine(140 * s);
        ImGui.pushStyleColor(ImGuiCol.Text, col(Theme.TextSecondary));
        ImGui.text(description);
        ImGui.popStyleColor();
    }
}
