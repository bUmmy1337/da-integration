package net.bummy1337.daintegrate.ui;

import imgui.ImGui;
import net.bummy1337.daintegrate.configurations.SettingsDto;

import java.io.IOException;
import java.util.function.Consumer;

class SettingsPage {
    private final DaMenu menu;
    private final UiContext context;
    private String error;
    private final float[] scaleSlider = new float[1];

    SettingsPage(DaMenu menu, UiContext context) {
        this.menu = menu;
        this.context = context;
    }

    void render() {
        float s = DaMenu.scale();
        Widgets.title(I18n.t("set.title"), I18n.t("set.subtitle"));
        var settings = context.getSettings();
        float width = Math.min(ImGui.getContentRegionAvailX(), 720 * s);

        if (settings != null && Widgets.beginCard("##behaviour", width, 0)) {
            toggle(I18n.t("set.skipTest"), I18n.t("set.skipTest.desc"), settings.skipTestDonation, v -> settings.skipTestDonation = v);
            Widgets.gap(4);
            toggle(I18n.t("conn.auto"), null, !settings.disableAutoConnect, v -> settings.disableAutoConnect = !v);
            Widgets.gap(4);
            toggle(I18n.t("set.welcome"), null, !settings.disableWelcomeMessage, v -> settings.disableWelcomeMessage = !v);
            Widgets.gap(8);

            ImGui.alignTextToFramePadding();
            ImGui.text(I18n.t("set.uiScale"));
            ImGui.sameLine(200 * s);
            scaleSlider[0] = settings.uiScale <= 0 ? 1f : settings.uiScale;
            ImGui.setNextItemWidth(220 * s);
            ImGui.sliderFloat("##scale", scaleSlider, 0.75f, 1.5f, "%.2fx");
            if (ImGui.isItemDeactivatedAfterEdit()) {
                settings.uiScale = Math.round(scaleSlider[0] * 20f) / 20f;
                save(settings);
            } else if (ImGui.isItemActive()) {
                settings.uiScale = scaleSlider[0];
            }
        }
        if (settings != null)
            Widgets.endCard();

        if (Widgets.beginCard("##files", width, 0)) {
            Widgets.heading(I18n.t("set.files"));
            ImGui.textDisabled(context.getSettingsFile().getAbsolutePath());
            Widgets.gap(2);
            if (Widgets.button(I18n.t("set.openConfig"), 0))
                context.openFile(context.getSettingsFile());
            ImGui.sameLine();
            if (Widgets.button(I18n.t("set.openFolder"), 0))
                context.openFile(context.getSettingsFile().getParentFile());
            ImGui.sameLine();
            if (Widgets.button(I18n.t("set.reload"), 0))
                context.reloadSettings();
        }
        Widgets.endCard();

        if (error != null)
            Widgets.colored(Theme.Danger, error);
    }

    private void toggle(String label, String description, boolean value, Consumer<Boolean> setter) {
        if (ImGui.checkbox(label, value)) {
            setter.accept(!value);
            save(context.getSettings());
        }
        if (description != null) {
            ImGui.indent(ImGui.getFrameHeight() + ImGui.getStyle().getItemInnerSpacingX());
            ImGui.textDisabled(description);
            ImGui.unindent(ImGui.getFrameHeight() + ImGui.getStyle().getItemInnerSpacingX());
        }
    }

    private void save(SettingsDto settings) {
        try {
            context.saveSettings(settings);
            error = null;
        } catch (IOException e) {
            error = I18n.t("trg.saveError", e.getMessage());
        }
    }
}
