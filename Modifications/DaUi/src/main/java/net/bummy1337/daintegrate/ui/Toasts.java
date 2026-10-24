package net.bummy1337.daintegrate.ui;

import imgui.ImDrawList;
import imgui.ImFont;
import imgui.ImGui;

import net.bummy1337.dontaionalerts.AlertType;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import static net.bummy1337.daintegrate.ui.Theme.col;
import static net.bummy1337.daintegrate.ui.Theme.white;

/**
 * Pill-shaped glass notifications in a corner of the screen, drawn over the game and over the menu.
 * {@link #push} can be called from any thread; everything else runs on the render thread.
 */
public final class Toasts {
    public enum Kind { Donation, Subscription, Twitch, Connected, Disconnected, Error, Info }

    public enum Corner {
        TopLeft, TopRight, BottomLeft, BottomRight;

        boolean right() {
            return this == TopRight || this == BottomRight;
        }

        boolean bottom() {
            return this == BottomLeft || this == BottomRight;
        }

        public static Corner parse(String value) {
            if (value != null)
                for (var corner : values())
                    if (corner.name().equalsIgnoreCase(value))
                        return corner;
            return TopRight;
        }
    }

    private static final float LifeSeconds = 6f;
    private static final float EnterSeconds = 0.35f;
    private static final float ExitSeconds = 0.3f;
    private static final int MaxVisible = 4;

    private static final ConcurrentLinkedQueue<Toast> incoming = new ConcurrentLinkedQueue<>();
    private static final List<Toast> shown = new ArrayList<>();

    private Toasts() {
    }

    /**
     * Queues a notification.
     *
     * @param title  main line, e.g. the donor's name
     * @param detail second line, may be null
     * @param value  highlighted text on the right (an amount), may be null
     */
    public static void push(Kind kind, String title, String detail, String value) {
        incoming.add(new Toast(kind, title, detail, value));
    }

    /** A notification for an alert from Donation Alerts. */
    public static void pushEvent(ReadOnlyDonationAlertsEvent event) {
        var type = event.getType();
        Kind kind = type == AlertType.Donate ? Kind.Donation
                : type == AlertType.TwitchBits || type == AlertType.TwitchPoints ? Kind.Twitch
                : Kind.Subscription;
        var name = event.getUserName() == null || event.getUserName().isBlank() ? I18n.t("toast.anonymous") : event.getUserName();
        var message = event.getMessage() == null || event.getMessage().isBlank() ? null : event.getMessage();
        var detail = message == null ? OverviewPage.typeName(type) : message;
        var amount = OverviewPage.formatAmount(event);
        push(kind, name, detail, "—".equals(amount) ? null : amount);
    }

    public static boolean isIdle() {
        return incoming.isEmpty() && shown.isEmpty();
    }

    public static void clear() {
        incoming.clear();
        shown.clear();
    }

    /** Draws the notifications on the foreground of the current ImGui frame. */
    public static void render(ImGuiHost host, float displayWidth, float displayHeight, float scale, Corner corner) {
        float now = (float) ImGui.getTime();
        for (Toast toast; (toast = incoming.poll()) != null; ) {
            toast.bornAt = now;
            shown.add(0, toast);
        }
        // Too many at once: the oldest ones leave early
        for (int i = MaxVisible; i < shown.size(); i++)
            shown.get(i).dismiss(now);
        shown.removeIf(toast -> toast.gone(now));
        if (shown.isEmpty())
            return;

        float s = scale;
        float margin = 16 * s;
        float gap = 8 * s;
        float height = 50 * s;
        var draw = ImGui.getForegroundDrawList();

        float offset = 0;
        for (var toast : shown) {
            float appear = ease(clamp((now - toast.bornAt) / EnterSeconds));
            float leave = toast.leftAt < 0 ? 0 : ease(clamp((now - toast.leftAt) / ExitSeconds));
            if (toast.leftAt < 0 && now - toast.bornAt > LifeSeconds)
                toast.dismiss(now);
            float alpha = appear * (1 - leave);

            // Slots glide when a toast above them appears or leaves
            toast.slot += (offset - toast.slot) * Math.min(1f, ImGui.getIO().getDeltaTime() * 12f);
            if (toast.bornAt == now)
                toast.slot = offset;

            float width = measure(host, toast, s);
            float slide = (1 - appear) * 24 * s + leave * 24 * s;
            float x = corner.right() ? displayWidth - margin - width + slide : margin - slide;
            float y = corner.bottom() ? displayHeight - margin - height - toast.slot : margin + toast.slot;
            drawToast(host, draw, toast, x, y, width, height, s, alpha, now);

            offset += (height + gap) * (1 - leave);
        }
    }

    private static float measure(ImGuiHost host, Toast toast, float s) {
        float text = Math.max(textWidth(host.bold(), toast.title), toast.detail == null ? 0 : textWidth(host.regular(), toast.detail));
        text = Math.min(text, 300 * s);
        float value = toast.value == null ? 0 : textWidth(host.bold(), toast.value) + 24 * s + 12 * s;
        return 7 * s + 36 * s + 12 * s + text + value + 20 * s;
    }

    private static void drawToast(ImGuiHost host, ImDrawList draw, Toast toast, float x, float y, float w, float h, float s,
                                  float alpha, float now) {
        int accent = accent(toast.kind);
        float r = h / 2;

        // Soft shadow, glass body, hairline border
        for (int i = 3; i >= 1; i--)
            draw.addRectFilled(x - i * 2 * s, y - i * s + 4 * s, x + w + i * 2 * s, y + h + i * 3 * s,
                    col(Theme.Black, 0.10f * alpha), r + i * 2 * s);
        draw.addRectFilled(x, y, x + w, y + h, col(Theme.Glass, 0.92f * alpha), r);
        draw.addRect(x, y, x + w, y + h, white(0.10f * alpha), r);

        // Icon tile
        float icon = 36 * s;
        float ix = x + 7 * s;
        float iy = y + (h - icon) / 2;
        draw.addCircleFilled(ix + icon / 2, iy + icon / 2, icon / 2, col(accent, 0.18f * alpha), 32);
        draw.addCircle(ix + icon / 2, iy + icon / 2, icon / 2, col(accent, 0.35f * alpha), 32, 1f);
        Icons.draw(draw, icon(toast.kind), ix + icon / 2, iy + icon / 2, icon * 0.36f, col(accent, alpha));

        // Text
        float tx = ix + icon + 12 * s;
        float valueWidth = toast.value == null ? 0 : textWidth(host.bold(), toast.value) + 24 * s;
        float maxText = x + w - tx - 20 * s - (valueWidth > 0 ? valueWidth + 12 * s : 0);
        var bold = host.bold();
        var regular = host.regular();
        if (toast.detail == null) {
            text(draw, bold, tx, y + (h - bold.getLegacySize()) / 2, white(Theme.TextAlpha * alpha), toast.title, maxText);
        } else {
            float block = bold.getLegacySize() + 2 * s + regular.getLegacySize();
            float ty = y + (h - block) / 2;
            text(draw, bold, tx, ty, white(Theme.TextAlpha * alpha), toast.title, maxText);
            text(draw, regular, tx, ty + bold.getLegacySize() + 2 * s, white(Theme.TextSecondaryAlpha * alpha), toast.detail, maxText);
        }

        // Amount pill
        if (toast.value != null) {
            float ph = 26 * s;
            float px = x + w - 12 * s - valueWidth;
            float py = y + (h - ph) / 2;
            draw.addRectFilled(px, py, px + valueWidth, py + ph, col(accent, alpha), ph / 2);
            text(draw, bold, px + 12 * s, py + (ph - bold.getLegacySize()) / 2, col(0x1A0E00, alpha), toast.value, valueWidth);
        }

        // Time left, as a thin line along the bottom edge
        if (toast.leftAt < 0) {
            float left = 1 - clamp((now - toast.bornAt) / LifeSeconds);
            float lx = x + r;
            float lw = (w - r * 2) * left;
            draw.addRectFilled(lx, y + h - 2 * s, lx + lw, y + h - 1 * s, col(accent, 0.45f * alpha), 1 * s);
        }
    }

    private static void text(ImDrawList draw, ImFont font, float x, float y, int color, String text, float maxWidth) {
        ImGui.pushFont(font, font.getLegacySize());
        draw.addText(x, y, color, clip(font, text, maxWidth));
        ImGui.popFont();
    }

    private static String clip(ImFont font, String text, float width) {
        if (text == null)
            return "";
        if (textWidth(font, text) <= width)
            return text;
        int end = text.length();
        while (end > 0 && textWidth(font, text.substring(0, end) + "…") > width)
            end--;
        return text.substring(0, end) + "…";
    }

    private static float textWidth(ImFont font, String text) {
        if (text == null)
            return 0;
        return font.calcTextSizeAX(font.getLegacySize(), Float.MAX_VALUE, 0, text);
    }

    private static int accent(Kind kind) {
        return switch (kind) {
            case Donation -> Theme.Accent;
            case Subscription -> Theme.Danger;
            case Twitch -> Theme.Purple;
            case Connected -> Theme.Success;
            case Disconnected -> Theme.TextSecondary;
            case Error -> Theme.Danger;
            case Info -> Theme.Info;
        };
    }

    private static Icons.Kind icon(Kind kind) {
        return switch (kind) {
            case Donation, Twitch -> Icons.Kind.Bolt;
            case Subscription -> Icons.Kind.Heart;
            case Connected, Disconnected, Error -> Icons.Kind.Plug;
            case Info -> Icons.Kind.Question;
        };
    }

    private static float clamp(float value) {
        return Math.max(0, Math.min(1, value));
    }

    /** Ease-out cubic, the iOS-like deceleration. */
    private static float ease(float t) {
        float inv = 1 - t;
        return 1 - inv * inv * inv;
    }

    private static final class Toast {
        final Kind kind;
        final String title;
        final String detail;
        final String value;
        float bornAt;
        float leftAt = -1;
        float slot;

        Toast(Kind kind, String title, String detail, String value) {
            this.kind = kind;
            this.title = title;
            this.detail = detail;
            this.value = value;
        }

        void dismiss(float now) {
            if (leftAt < 0)
                leftAt = now;
        }

        boolean gone(float now) {
            return leftAt >= 0 && now - leftAt > ExitSeconds;
        }
    }
}
