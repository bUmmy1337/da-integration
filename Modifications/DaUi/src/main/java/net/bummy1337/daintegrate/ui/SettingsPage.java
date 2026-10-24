package net.bummy1337.daintegrate.ui;

import imgui.ImGui;
import net.bummy1337.daintegrate.configurations.SettingsDto;

import java.io.IOException;
import java.util.function.Consumer;

class SettingsPage {
    private final DaMenu menu;
    private final UiContext context;
    private String error;

    SettingsPage(DaMenu menu, UiContext context) {
        this.menu = menu;
        this.context = context;
    }

    void render() {
        float s = DaMenu.scale();
        Widgets.title(I18n.t("set.title"), I18n.t("set.subtitle"));
        var settings = context.getSettings();
        float spacing = ImGui.getStyle().getItemSpacingX();
        float available = Math.min(ImGui.getContentRegionAvailX(), 900 * s);
        float column = (available - spacing) / 2;

        ImGui.beginGroup();
        if (settings != null) {
            if (Widgets.beginGroup("##behaviour", I18n.t("set.behaviour"), column)) {
                toggle(I18n.t("set.skipTest"), I18n.t("set.skipTest.desc"), settings.skipTestDonation, v -> settings.skipTestDonation = v);
                Widgets.gap(6);
                toggle(I18n.t("conn.auto"), null, !settings.disableAutoConnect, v -> settings.disableAutoConnect = !v);
                Widgets.gap(6);
                toggle(I18n.t("set.welcome"), null, !settings.disableWelcomeMessage, v -> settings.disableWelcomeMessage = !v);
            }
            Widgets.endCard();

            if (Widgets.beginGroup("##toasts", I18n.t("set.toasts"), column)) {
                toggle(I18n.t("set.toasts.enabled"), I18n.t("set.toasts.desc"), !settings.disableToasts, v -> settings.disableToasts = !v);
                Widgets.gap(6);
                ImGui.beginDisabled(settings.disableToasts);
                ImGui.text(I18n.t("set.toasts.position"));
                var corner = Toasts.Corner.parse(settings.toastCorner);
                float half = (ImGui.getContentRegionAvailX() - spacing) / 2;
                int vertical = Widgets.segmented("##toast-v", new String[]{I18n.t("seg.top"), I18n.t("seg.bottom")}, corner.name().startsWith("Bottom") ? 1 : 0, half);
                ImGui.sameLine();
                int horizontal = Widgets.segmented("##toast-h", new String[]{I18n.t("seg.left"), I18n.t("seg.right")}, corner.name().endsWith("Right") ? 1 : 0, half);
                var picked = Toasts.Corner.valueOf((vertical == 1 ? "Bottom" : "Top") + (horizontal == 1 ? "Right" : "Left"));
                if (picked != corner) {
                    settings.toastCorner = picked.name();
                    save(settings);
                    sample();
                }
                Widgets.gap(4);
                if (Widgets.button(I18n.t("set.toasts.test"), -1))
                    sample();
                ImGui.endDisabled();
            }
            Widgets.endCard();
        }
        ImGui.endGroup();
        ImGui.sameLine();

        ImGui.beginGroup();
        if (settings != null) {
            if (Widgets.beginGroup("##appearance", I18n.t("set.appearance"), column)) {
                ImGui.text(I18n.t("set.uiScale"));
                ImGui.sameLine(ImGui.getContentRegionMaxX() - ImGui.calcTextSizeX("1.50x"));
                ImGui.textDisabled(String.format("%.2fx", settings.uiScale <= 0 ? 1f : settings.uiScale));
                float current = settings.uiScale <= 0 ? 1f : settings.uiScale;
                float value = Widgets.slider("##scale", current, 0.75f, 1.5f, ImGui.getContentRegionAvailX());
                if (ImGui.isItemDeactivated()) {
                    settings.uiScale = Math.round(value * 20f) / 20f;
                    save(settings);
                } else if (ImGui.isItemActive()) {
                    settings.uiScale = value;
                }
            }
            Widgets.endCard();
        }

        if (Widgets.beginGroup("##files", I18n.t("set.files"), column)) {
            ImGui.pushTextWrapPos(0);
            ImGui.textDisabled(context.getSettingsFile().getAbsolutePath());
            ImGui.popTextWrapPos();
            Widgets.gap(4);
            float half = (ImGui.getContentRegionAvailX() - spacing) / 2;
            if (Widgets.button(I18n.t("set.openConfig"), half))
                context.openFile(context.getSettingsFile());
            ImGui.sameLine();
            if (Widgets.button(I18n.t("set.openFolder"), half))
                context.openFile(context.getSettingsFile().getParentFile());
            if (Widgets.button(I18n.t("set.reload"), -1))
                context.reloadSettings();
        }
        Widgets.endCard();
        ImGui.endGroup();

        if (error != null)
            Widgets.colored(Theme.Danger, error);
    }

    private static void sample() {
        Toasts.push(Toasts.Kind.Donation, I18n.t("toast.sample.name"), I18n.t("toast.sample.message"), "500 RUB");
    }

    private void toggle(String label, String description, boolean value, Consumer<Boolean> setter) {
        if (Widgets.toggleRow(label, description, value)) {
            setter.accept(!value);
            save(context.getSettings());
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
