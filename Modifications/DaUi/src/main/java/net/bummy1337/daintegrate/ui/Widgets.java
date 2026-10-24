package net.bummy1337.daintegrate.ui;

import imgui.ImFont;
import imgui.ImGui;
import imgui.flag.ImGuiChildFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiMouseCursor;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import imgui.type.ImString;
import net.bummy1337.dontaionalerts.ConnectionState;

import static net.bummy1337.daintegrate.ui.Theme.col;

/**
 * Small building blocks shared by the menu pages.
 */
final class Widgets {
    private Widgets() {
    }

    static float s() {
        return DaMenu.scale();
    }

    static void title(String title, String subtitle) {
        font(DaMenu.fonts().title(), () -> ImGui.text(title));
        if (subtitle != null)
            muted(subtitle);
        ImGui.dummy(0, 6 * s());
    }

    static void heading(String text) {
        font(DaMenu.fonts().heading(), () -> ImGui.text(text));
    }

    static void bold(String text) {
        font(DaMenu.fonts().bold(), () -> ImGui.text(text));
    }

    static void muted(String text) {
        ImGui.pushStyleColor(ImGuiCol.Text, col(Theme.TextSecondary));
        ImGui.textWrapped(text);
        ImGui.popStyleColor();
    }

    static void colored(int rgb, String text) {
        ImGui.pushStyleColor(ImGuiCol.Text, col(rgb));
        ImGui.textWrapped(text);
        ImGui.popStyleColor();
    }

    static void font(ImFont font, Runnable body) {
        ImGui.pushFont(font, font.getLegacySize());
        body.run();
        ImGui.popFont();
    }

    /** Filled accent button. */
    static boolean primaryButton(String label, float width) {
        ImGui.pushStyleColor(ImGuiCol.Button, col(Theme.Accent));
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, col(Theme.AccentHover));
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, col(Theme.AccentActive));
        ImGui.pushStyleColor(ImGuiCol.Text, col(0xFFFFFF));
        boolean clicked = font(DaMenu.fonts().bold(), label, width);
        ImGui.popStyleColor(4);
        handCursor();
        return clicked;
    }

    static boolean dangerButton(String label, float width) {
        ImGui.pushStyleColor(ImGuiCol.Button, col(Theme.Danger, 0.14f));
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, col(Theme.Danger, 0.26f));
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, col(Theme.Danger, 0.36f));
        ImGui.pushStyleColor(ImGuiCol.Text, col(Theme.Danger));
        boolean clicked = ImGui.button(label, width, 0);
        ImGui.popStyleColor(4);
        handCursor();
        return clicked;
    }

    static boolean button(String label, float width) {
        boolean clicked = ImGui.button(label, width, 0);
        handCursor();
        return clicked;
    }

    /** Borderless text button that looks like a link. */
    static boolean link(String label) {
        ImGui.pushStyleColor(ImGuiCol.Text, col(Theme.Accent));
        ImGui.text(label);
        ImGui.popStyleColor();
        boolean hovered = ImGui.isItemHovered();
        if (hovered) {
            var min = ImGui.getItemRectMin();
            var max = ImGui.getItemRectMax();
            ImGui.getWindowDrawList().addLine(min.x, max.y, max.x, max.y, col(Theme.Accent), 1f);
            ImGui.setMouseCursor(ImGuiMouseCursor.Hand);
        }
        return hovered && ImGui.isMouseClicked(0);
    }

    static boolean beginCard(String id, float width, float height) {
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 18 * s(), 16 * s());
        boolean open = ImGui.beginChild(id, width, height, ImGuiChildFlags.Borders | (height == 0 ? ImGuiChildFlags.AutoResizeY : 0) | ImGuiChildFlags.AlwaysUseWindowPadding,
                ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse);
        ImGui.popStyleVar();
        return open;
    }

    static void endCard() {
        ImGui.endChild();
    }

    static void statusDot(int rgb, boolean pulse) {
        statusDot(rgb, pulse, false);
    }

    /** With {@code frameLine} the dot is centred on a line that holds buttons or inputs. */
    static void statusDot(int rgb, boolean pulse, boolean frameLine) {
        var draw = ImGui.getWindowDrawList();
        var pos = ImGui.getCursorScreenPos();
        float r = 4.5f * s();
        float lineHeight = frameLine ? ImGui.getFrameHeight() : ImGui.getTextLineHeight();
        float cx = pos.x + r;
        float cy = pos.y + lineHeight / 2;
        if (pulse) {
            float t = (float) ((ImGui.getTime() * 1.6) % 1.0);
            draw.addCircleFilled(cx, cy, r + t * 6 * s(), col(rgb, 0.35f * (1 - t)));
        }
        draw.addCircleFilled(cx, cy, r, col(rgb));
        ImGui.dummy(r * 2 + 4 * s(), lineHeight);
        ImGui.sameLine();
    }

    /** Rounded label with a tinted background. */
    static void badge(String text, int rgb) {
        var draw = ImGui.getWindowDrawList();
        var pos = ImGui.getCursorScreenPos();
        float padX = 8 * s();
        float padY = 2 * s();
        float w = ImGui.calcTextSizeX(text) + padX * 2;
        float h = ImGui.getTextLineHeight() + padY * 2;
        draw.addRectFilled(pos.x, pos.y, pos.x + w, pos.y + h, col(rgb, 0.16f), h / 2);
        draw.addText(pos.x + padX, pos.y + padY, col(rgb), text);
        ImGui.dummy(w, h);
    }

    /** Two-tone pill used in the header for the connection state. */
    static void statusPill(ConnectionState state) {
        int rgb = stateColor(state);
        var label = I18n.t("state." + state.name());
        var draw = ImGui.getWindowDrawList();
        var pos = ImGui.getCursorScreenPos();
        float padX = 12 * s();
        float dot = 4 * s();
        float h = ImGui.getFrameHeight();
        float w = padX * 2 + dot * 2 + 8 * s() + ImGui.calcTextSizeX(label);
        draw.addRectFilled(pos.x, pos.y, pos.x + w, pos.y + h, col(rgb, 0.14f), h / 2);
        float cx = pos.x + padX + dot;
        float cy = pos.y + h / 2;
        if (state == ConnectionState.Connecting || state == ConnectionState.Reconnecting || state == ConnectionState.Connected) {
            float t = (float) ((ImGui.getTime() * (state == ConnectionState.Connected ? 0.8 : 1.8)) % 1.0);
            draw.addCircleFilled(cx, cy, dot + t * 5 * s(), col(rgb, 0.4f * (1 - t)));
        }
        draw.addCircleFilled(cx, cy, dot, col(rgb));
        draw.addText(cx + dot + 8 * s(), pos.y + (h - ImGui.getTextLineHeight()) / 2, col(rgb), label);
        ImGui.dummy(w, h);
    }

    static int stateColor(ConnectionState state) {
        return switch (state) {
            case Connected -> Theme.Success;
            case Connecting, Reconnecting -> Theme.Warning;
            case Error -> Theme.Danger;
            default -> Theme.TextMuted;
        };
    }

    /** Numbered circle for wizard steps; shows a check mark once done. */
    static void stepNumber(int number, boolean done, boolean current) {
        var draw = ImGui.getWindowDrawList();
        var pos = ImGui.getCursorScreenPos();
        float r = 14 * s();
        float cx = pos.x + r;
        float cy = pos.y + r;
        if (done) {
            draw.addCircleFilled(cx, cy, r, col(Theme.Success, 0.18f));
            float k = r * 0.45f;
            draw.addLine(cx - k, cy, cx - k * 0.25f, cy + k * 0.7f, col(Theme.Success), 2.2f * s());
            draw.addLine(cx - k * 0.25f, cy + k * 0.7f, cx + k, cy - k * 0.6f, col(Theme.Success), 2.2f * s());
        } else {
            int rgb = current ? Theme.Accent : Theme.TextMuted;
            draw.addCircleFilled(cx, cy, r, col(rgb, current ? 0.2f : 0.12f));
            var text = String.valueOf(number);
            ImGui.pushFont(DaMenu.fonts().bold(), DaMenu.fonts().bold().getLegacySize());
            float tw = ImGui.calcTextSizeX(text);
            draw.addText(cx - tw / 2, cy - ImGui.getTextLineHeight() / 2, col(rgb), text);
            ImGui.popFont();
        }
        ImGui.dummy(r * 2, r * 2);
    }

    /**
     * Single-line text input bound to a String. Returns the new value (the same instance if unchanged).
     */
    static String input(String id, String hint, String value, float width, int capacity) {
        return input(id, hint, value, width, capacity, ImGuiInputTextFlags.None);
    }

    static String input(String id, String hint, String value, float width, int capacity, int flags) {
        var buffer = new ImString(value == null ? "" : value, Math.max(capacity, value == null ? 0 : value.length() + 16));
        ImGui.setNextItemWidth(width);
        boolean changed = hint == null
                ? ImGui.inputText(id, buffer, flags)
                : ImGui.inputTextWithHint(id, hint, buffer, flags);
        return changed ? buffer.get() : value;
    }

    static void tooltip(String text) {
        if (ImGui.isItemHovered())
            ImGui.setTooltip(text);
    }

    static void handCursor() {
        if (ImGui.isItemHovered())
            ImGui.setMouseCursor(ImGuiMouseCursor.Hand);
    }

    static void gap(float height) {
        ImGui.dummy(0, height * s());
    }

    static String ago(long timestamp) {
        long seconds = Math.max(0, (System.currentTimeMillis() - timestamp) / 1000);
        if (seconds < 60)
            return I18n.t("ago.s", seconds);
        if (seconds < 3600)
            return I18n.t("ago.m", seconds / 60);
        return I18n.t("ago.h", seconds / 3600);
    }

    private static boolean font(ImFont font, String label, float width) {
        ImGui.pushFont(font, font.getLegacySize());
        boolean clicked = ImGui.button(label, width, 0);
        ImGui.popFont();
        return clicked;
    }
}
