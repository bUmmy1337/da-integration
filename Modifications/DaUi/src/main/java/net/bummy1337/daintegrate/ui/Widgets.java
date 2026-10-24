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

import java.util.HashMap;
import java.util.Map;

import static net.bummy1337.daintegrate.ui.Theme.col;
import static net.bummy1337.daintegrate.ui.Theme.white;

/**
 * Building blocks shared by the menu pages: glass cards, iOS-style switches,
 * segmented controls, pill buttons and small typographic helpers.
 */
final class Widgets {
    /** Animation progress per widget id, so switches and segments slide instead of jumping. */
    private static final Map<Integer, Float> animations = new HashMap<>();

    private Widgets() {
    }

    static float s() {
        return DaMenu.scale();
    }

    // ---------------------------------------------------------------- text

    /** Big lowercase page title ending with a dot, like "триггеры." */
    static void title(String title, String subtitle) {
        font(DaMenu.fonts().title(), () -> ImGui.text(title.toLowerCase() + "."));
        if (subtitle != null) {
            ImGui.setCursorPosY(ImGui.getCursorPosY() - 4 * s());
            muted(subtitle);
        }
        ImGui.dummy(0, 8 * s());
    }

    static void heading(String text) {
        font(DaMenu.fonts().heading(), () -> ImGui.text(text));
    }

    static void bold(String text) {
        font(DaMenu.fonts().bold(), () -> ImGui.text(text));
    }

    /** Small lowercase caption, used for sidebar sections and card labels. */
    static void caption(String text) {
        ImGui.pushStyleColor(ImGuiCol.Text, white(Theme.TextMutedAlpha));
        ImGui.text(text.toLowerCase());
        ImGui.popStyleColor();
    }

    static void muted(String text) {
        ImGui.pushStyleColor(ImGuiCol.Text, white(Theme.TextSecondaryAlpha));
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

    // ---------------------------------------------------------------- buttons

    /** Light pill button, the main call to action (like "Play for free"). */
    static boolean primaryButton(String label, float width) {
        ImGui.pushStyleColor(ImGuiCol.Button, col(Theme.Accent));
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, col(Theme.AccentLight));
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, col(Theme.AccentDark));
        ImGui.pushStyleColor(ImGuiCol.Text, col(0x1A0E00));
        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 100);
        boolean clicked = boldButton(label, width);
        ImGui.popStyleVar();
        ImGui.popStyleColor(4);
        handCursor();
        return clicked;
    }

    /** Dark translucent pill button (like "See plans"). */
    static boolean button(String label, float width) {
        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 100);
        ImGui.pushStyleVar(ImGuiStyleVar.FrameBorderSize, 1);
        ImGui.pushStyleColor(ImGuiCol.Border, white(0.10f));
        boolean clicked = ImGui.button(label, width, 0);
        ImGui.popStyleColor();
        ImGui.popStyleVar(2);
        handCursor();
        return clicked;
    }

    static boolean dangerButton(String label, float width) {
        ImGui.pushStyleColor(ImGuiCol.Button, col(Theme.Danger, 0.12f));
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, col(Theme.Danger, 0.22f));
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, col(Theme.Danger, 0.32f));
        ImGui.pushStyleColor(ImGuiCol.Text, col(Theme.Danger));
        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, 100);
        boolean clicked = ImGui.button(label, width, 0);
        ImGui.popStyleVar();
        ImGui.popStyleColor(4);
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

    // ---------------------------------------------------------------- controls

    /** iOS-style switch. Returns true when clicked. */
    static boolean toggle(String id, boolean value) {
        float s = s();
        float height = 22 * s;
        float width = 38 * s;
        var draw = ImGui.getWindowDrawList();
        var pos = ImGui.getCursorScreenPos();
        float y = pos.y + (ImGui.getFrameHeight() - height) / 2;
        boolean clicked = ImGui.invisibleButton(id, width, ImGui.getFrameHeight());
        boolean hovered = ImGui.isItemHovered();
        handCursor();

        float t = animate(ImGui.getID(id), value ? 1f : 0f);
        int track = blend(white(hovered ? 0.16f : 0.12f), col(Theme.Accent), t);
        draw.addRectFilled(pos.x, y, pos.x + width, y + height, track, height / 2);
        float knob = height / 2 - 2.5f * s;
        float knobX = pos.x + height / 2 + t * (width - height);
        draw.addCircleFilled(knobX, y + height / 2 + 1 * s, knob, col(Theme.Black, 0.25f));
        draw.addCircleFilled(knobX, y + height / 2, knob, col(Theme.White));
        return clicked;
    }

    /** Label on the left, switch on the right; the whole row spans the available width. */
    static boolean toggleRow(String label, String description, boolean value) {
        float s = s();
        float switchWidth = 38 * s;
        float start = ImGui.getCursorPosX();
        float available = ImGui.getContentRegionAvailX();
        ImGui.alignTextToFramePadding();
        ImGui.text(label);
        ImGui.sameLine(start + available - switchWidth);
        boolean clicked = toggle("##" + label, value);
        if (description != null) {
            ImGui.setCursorPosY(ImGui.getCursorPosY() - 6 * s);
            ImGui.pushStyleColor(ImGuiCol.Text, white(Theme.TextMutedAlpha));
            ImGui.pushTextWrapPos(ImGui.getCursorPosX() + available - switchWidth - 20 * s);
            ImGui.textWrapped(description);
            ImGui.popTextWrapPos();
            ImGui.popStyleColor();
        }
        return clicked;
    }

    /** Pill segmented control (like "pistol / smg / rifle"). Returns the selected index. */
    static int segmented(String id, String[] labels, int selected, float width) {
        float s = s();
        var draw = ImGui.getWindowDrawList();
        var pos = ImGui.getCursorScreenPos();
        float height = ImGui.getFrameHeight() + 6 * s;
        float pad = 3 * s;
        float segment = (width - pad * 2) / labels.length;

        draw.addRectFilled(pos.x, pos.y, pos.x + width, pos.y + height, white(0.035f), height / 2);
        draw.addRect(pos.x, pos.y, pos.x + width, pos.y + height, white(0.08f), height / 2);

        float t = animate(ImGui.getID(id), selected);
        float pillX = pos.x + pad + t * segment;
        draw.addRectFilled(pillX, pos.y + pad, pillX + segment, pos.y + height - pad, white(0.10f), (height - pad * 2) / 2);
        draw.addRect(pillX, pos.y + pad, pillX + segment, pos.y + height - pad, white(0.10f), (height - pad * 2) / 2);

        int result = selected;
        ImGui.pushID(id);
        for (int i = 0; i < labels.length; i++) {
            float x = pos.x + pad + i * segment;
            ImGui.setCursorScreenPos(x, pos.y);
            if (ImGui.invisibleButton("##seg" + i, segment, height))
                result = i;
            boolean hovered = ImGui.isItemHovered();
            if (hovered)
                ImGui.setMouseCursor(ImGuiMouseCursor.Hand);
            var label = labels[i];
            float textWidth = ImGui.calcTextSizeX(label);
            int color = i == selected ? white(Theme.TextAlpha) : white(hovered ? 0.75f : Theme.TextSecondaryAlpha);
            draw.addText(x + (segment - textWidth) / 2, pos.y + (height - ImGui.getTextLineHeight()) / 2, color, label);
        }
        ImGui.popID();
        ImGui.setCursorScreenPos(pos.x, pos.y);
        ImGui.dummy(width, height);
        return result;
    }

    /** Thin iOS-style slider: hairline track, accent fill and a round white knob. Returns the new value. */
    static float slider(String id, float value, float min, float max, float width) {
        float s = s();
        var draw = ImGui.getWindowDrawList();
        var pos = ImGui.getCursorScreenPos();
        float height = ImGui.getFrameHeight();
        float knob = 9 * s;
        ImGui.invisibleButton(id, width, height);
        boolean active = ImGui.isItemActive();
        boolean hovered = ImGui.isItemHovered();
        handCursor();
        float left = pos.x + knob;
        float right = pos.x + width - knob;
        if (active) {
            float t = Math.max(0, Math.min(1, (ImGui.getMousePosX() - left) / (right - left)));
            value = min + t * (max - min);
        }
        float t = (value - min) / (max - min);
        float cy = pos.y + height / 2;
        float x = left + t * (right - left);
        float track = 2 * s;
        draw.addRectFilled(left, cy - track, right, cy + track, white(0.12f), track);
        draw.addRectFilled(left, cy - track, x, cy + track, col(Theme.Accent), track);
        draw.addCircleFilled(x, cy + 1 * s, knob, col(Theme.Black, 0.3f));
        draw.addCircleFilled(x, cy, knob * (active ? 1.08f : 1f), col(Theme.White, hovered || active ? 1f : 0.95f));
        return value;
    }

    // ---------------------------------------------------------------- containers

    /** Glass card. Pass height 0 to fit the content. */
    static boolean beginCard(String id, float width, float height) {
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 18 * s(), 16 * s());
        ImGui.pushStyleColor(ImGuiCol.ChildBg, white(Theme.CardAlpha));
        ImGui.pushStyleColor(ImGuiCol.Border, white(Theme.BorderAlpha));
        boolean open = ImGui.beginChild(id, width, height, ImGuiChildFlags.Borders | (height == 0 ? ImGuiChildFlags.AutoResizeY : 0) | ImGuiChildFlags.AlwaysUseWindowPadding,
                ImGuiWindowFlags.NoScrollbar | ImGuiWindowFlags.NoScrollWithMouse);
        ImGui.popStyleColor(2);
        ImGui.popStyleVar();
        return open;
    }

    /** Card with a centred bold header and a hairline under it (like "ragebot" / "accuracy"). */
    static boolean beginGroup(String id, String header, float width) {
        boolean open = beginCard(id, width, 0);
        var text = header.toLowerCase();
        ImGui.pushFont(DaMenu.fonts().bold(), DaMenu.fonts().bold().getLegacySize());
        float textWidth = ImGui.calcTextSizeX(text);
        ImGui.setCursorPosX((ImGui.getWindowWidth() - textWidth) / 2);
        ImGui.text(text);
        ImGui.popFont();
        hairline(-18 * s());
        return open;
    }

    static void endCard() {
        ImGui.endChild();
    }

    /** Full-width separator; a negative inset reaches the card edges. */
    static void hairline(float inset) {
        var draw = ImGui.getWindowDrawList();
        var pos = ImGui.getCursorScreenPos();
        float width = ImGui.getContentRegionAvailX();
        float y = pos.y + 2 * s();
        draw.addLine(pos.x + inset, y, pos.x + width - inset, y, white(0.07f), 1f);
        ImGui.dummy(0, 6 * s());
    }

    /** Separator that fades out at both ends. */
    static void fadeLine() {
        var draw = ImGui.getWindowDrawList();
        var pos = ImGui.getCursorScreenPos();
        float width = ImGui.getContentRegionAvailX();
        float half = width / 2;
        draw.addRectFilledMultiColor(pos.x, pos.y, pos.x + half, pos.y + 1, white(0), white(0.1f), white(0.1f), white(0));
        draw.addRectFilledMultiColor(pos.x + half, pos.y, pos.x + width, pos.y + 1, white(0.1f), white(0), white(0), white(0.1f));
        ImGui.dummy(0, 4 * s());
    }

    // ---------------------------------------------------------------- status

    static void statusDot(int rgb, boolean pulse) {
        statusDot(rgb, pulse, false);
    }

    /** With {@code frameLine} the dot is centred on a line that holds buttons or inputs. */
    static void statusDot(int rgb, boolean pulse, boolean frameLine) {
        var draw = ImGui.getWindowDrawList();
        var pos = ImGui.getCursorScreenPos();
        float r = 3.5f * s();
        float lineHeight = frameLine ? ImGui.getFrameHeight() : ImGui.getTextLineHeight();
        float cx = pos.x + r;
        float cy = pos.y + lineHeight / 2;
        if (pulse) {
            float t = (float) ((ImGui.getTime() * 1.2) % 1.0);
            draw.addCircleFilled(cx, cy, r + t * 6 * s(), col(rgb, 0.4f * (1 - t)));
        }
        draw.addCircleFilled(cx, cy, r, col(rgb));
        ImGui.dummy(r * 2 + 6 * s(), lineHeight);
        ImGui.sameLine();
    }

    /** Small uppercase tag with a tinted background (like "ACTIVE"). */
    static void badge(String text, int rgb) {
        var draw = ImGui.getWindowDrawList();
        var pos = ImGui.getCursorScreenPos();
        float padX = 8 * s();
        float padY = 2 * s();
        float w = ImGui.calcTextSizeX(text) + padX * 2;
        float h = ImGui.getTextLineHeight() + padY * 2;
        draw.addRectFilled(pos.x, pos.y, pos.x + w, pos.y + h, col(rgb, 0.14f), 6 * s());
        draw.addRect(pos.x, pos.y, pos.x + w, pos.y + h, col(rgb, 0.25f), 6 * s());
        draw.addText(pos.x + padX, pos.y + padY, col(rgb), text);
        ImGui.dummy(w, h);
    }

    /** Pill with a dot showing the connection state. */
    static void statusPill(ConnectionState state) {
        int rgb = stateColor(state);
        var label = I18n.t("state." + state.name()).toLowerCase();
        var draw = ImGui.getWindowDrawList();
        var pos = ImGui.getCursorScreenPos();
        float padX = 12 * s();
        float dot = 3.5f * s();
        float h = ImGui.getFrameHeight();
        float w = padX * 2 + dot * 2 + 8 * s() + ImGui.calcTextSizeX(label);
        draw.addRectFilled(pos.x, pos.y, pos.x + w, pos.y + h, col(rgb, 0.10f), h / 2);
        draw.addRect(pos.x, pos.y, pos.x + w, pos.y + h, col(rgb, 0.22f), h / 2);
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
            default -> Theme.TextSecondary;
        };
    }

    /** Numbered circle for wizard steps; shows a check mark once done. */
    static void stepNumber(int number, boolean done, boolean current) {
        var draw = ImGui.getWindowDrawList();
        var pos = ImGui.getCursorScreenPos();
        float r = 13 * s();
        float cx = pos.x + r;
        float cy = pos.y + r;
        if (done) {
            draw.addCircleFilled(cx, cy, r, col(Theme.Success, 0.15f));
            draw.addCircle(cx, cy, r, col(Theme.Success, 0.35f), 0, 1f);
            float k = r * 0.42f;
            draw.addLine(cx - k, cy, cx - k * 0.25f, cy + k * 0.7f, col(Theme.Success), 2f * s());
            draw.addLine(cx - k * 0.25f, cy + k * 0.7f, cx + k, cy - k * 0.6f, col(Theme.Success), 2f * s());
        } else {
            if (current)
                draw.addCircleFilled(cx, cy, r, col(Theme.Accent));
            else
                draw.addCircle(cx, cy, r, white(0.2f), 0, 1f);
            var text = String.valueOf(number);
            ImGui.pushFont(DaMenu.fonts().bold(), DaMenu.fonts().bold().getLegacySize());
            float tw = ImGui.calcTextSizeX(text);
            draw.addText(cx - tw / 2, cy - ImGui.getTextLineHeight() / 2, current ? col(0x1A0E00) : white(0.5f), text);
            ImGui.popFont();
        }
        ImGui.dummy(r * 2, r * 2);
    }

    /** Keyboard key cap (like "INSERT"). */
    static void keycap(String key) {
        var draw = ImGui.getWindowDrawList();
        var pos = ImGui.getCursorScreenPos();
        float padX = 7 * s();
        float w = ImGui.calcTextSizeX(key) + padX * 2;
        float h = ImGui.getTextLineHeight() + 4 * s();
        draw.addRectFilled(pos.x, pos.y, pos.x + w, pos.y + h, white(0.06f), 5 * s());
        draw.addRect(pos.x, pos.y, pos.x + w, pos.y + h, white(0.16f), 5 * s());
        draw.addLine(pos.x + 3 * s(), pos.y + h, pos.x + w - 3 * s(), pos.y + h, white(0.16f), 1.5f * s());
        draw.addText(pos.x + padX, pos.y + 2 * s(), white(0.8f), key);
        ImGui.dummy(w, h);
    }

    // ---------------------------------------------------------------- inputs

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

    // ---------------------------------------------------------------- helpers

    /** Eases a stored value towards the target, framerate-independent. */
    private static float animate(int id, float target) {
        float current = animations.getOrDefault(id, target);
        float speed = Math.min(1f, ImGui.getIO().getDeltaTime() * 14f);
        current += (target - current) * speed;
        if (Math.abs(target - current) < 0.001f)
            current = target;
        animations.put(id, current);
        return current;
    }

    /** Linear blend of two ABGR colours. */
    private static int blend(int from, int to, float t) {
        int result = 0;
        for (int shift = 0; shift < 32; shift += 8) {
            int a = (from >>> shift) & 0xFF;
            int b = (to >>> shift) & 0xFF;
            result |= (Math.round(a + (b - a) * t) & 0xFF) << shift;
        }
        return result;
    }

    private static boolean boldButton(String label, float width) {
        var font = DaMenu.fonts().bold();
        ImGui.pushFont(font, font.getLegacySize());
        boolean clicked = ImGui.button(label, width, 0);
        ImGui.popFont();
        return clicked;
    }
}
