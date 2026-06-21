package net.bummy1337.daintegrate.fabric.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public class Theme {
    public static final int
            BG_DARK = 0xF0101012,
            BG_MAIN = 0xF0161619,
            BG_PANEL = 0xF01E1E22,
            BG_PANEL_HOVER = 0xF028282D,
            BG_ENTRY = 0xF026262B,
            BG_ENTRY_HOVER = 0xF0313137,
            BG_INPUT = 0xF018181B,
            BG_INPUT_FOCUS = 0xF0222226,
            BORDER = 0xFF3A3A3F,
            BORDER_HOVER = 0xFF595961,
            BORDER_FOCUS = 0xFF8B8D95,
            ACCENT = 0xFF8B8D92,
            ACCENT_HOVER = 0xFFA4A7AC,
            ACCENT_TRANSPARENT = 0x604D5057,
            TEXT_PRIMARY = 0xFFDCDDDE,
            TEXT_SECONDARY = 0xFFB9BBBE,
            TEXT_MUTED = 0xFF72767D,
            TEXT_ACCENT = 0xFFE4E6EB,
            GREEN = 0xFFA4A7AC,
            GREEN_TRANSPARENT = 0x60707478,
            RED = 0xFF50545A,
            RED_TRANSPARENT = 0x6050555C,
            YELLOW = 0xFF8E9196,
            WHITE = 0xFFFFFFFF,
            BLACK = 0xFF000000,
            DIVIDER = 0xFF2B2B30,
            SCROLLBAR = 0x807A7D86,
            SCROLLBAR_HOVER = 0xFFE4E6EB;

    public static final int TITLE_BAR_H = 28;
    public static final int TAB_BAR_H = 30;
    public static final int STATUS_BAR_H = 22;
    public static final int PADDING = 8;
    public static final int CARD_RADIUS = 0;
    public static final int SCROLLBAR_WIDTH = 5;

    public static void drawSeparator(GuiGraphicsExtractor g, int x, int y, int w) {
        g.horizontalLine(x, x + w, y, DIVIDER);
    }

    public static void drawLeftAccentBar(GuiGraphicsExtractor g, int x, int y, int h, int color) {
        g.fill(x, y, x + 3, y + h, color);
    }

    public static void drawStatusDot(GuiGraphicsExtractor g, int x, int y, int color) {
        g.fill(x, y, x + 7, y + 7, color);
    }

    public static int withAlpha(int color, float alphaMultiplier) {
        int a = (int) ((color >> 24 & 0xFF) * alphaMultiplier);
        return (color & 0x00FFFFFF) | (a << 24);
    }

    public static int withAlpha(int color, int alpha) {
        return (color & 0x00FFFFFF) | (alpha << 24);
    }

    public static void fillRounded(GuiGraphicsExtractor g, int x, int y, int w, int h, int radius, int color) {
        int r = Math.max(0, Math.min(radius, Math.min(w, h) / 2));
        if (r <= 1) {
            g.fill(x, y, x + w, y + h, color);
            return;
        }
        g.fill(x + r, y, x + w - r, y + h, color);
        g.fill(x, y + r, x + w, y + h - r, color);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int dx = (int) Math.ceil(r - Math.sqrt(Math.max(0, r * r - dy * dy)));
            g.fill(x + dx, y + i, x + w - dx, y + i + 1, color);
            g.fill(x + dx, y + h - i - 1, x + w - dx, y + h - i, color);
        }
    }

    public static void fillCard(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean hovered) {
        int bg = hovered ? BG_ENTRY_HOVER : BG_ENTRY;
        fillRounded(g, x, y, w, h, 6, bg);
        g.outline(x, y, w, h, hovered ? BORDER_HOVER : BORDER);
    }

    public static void fillPanel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        fillRounded(g, x, y, w, h, 6, BG_PANEL);
        g.outline(x, y, w, h, BORDER);
    }

    public static void fillPanelGradient(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fillGradient(x, y, x + w, y + h, BG_PANEL, BG_MAIN);
        g.outline(x, y, w, h, BORDER);
    }

    public static void fillInput(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean focused) {
        fillRounded(g, x, y, w, h, 4, focused ? BG_INPUT_FOCUS : BG_INPUT);
        g.outline(x, y, w, h, focused ? BORDER_FOCUS : BORDER);
    }
}
