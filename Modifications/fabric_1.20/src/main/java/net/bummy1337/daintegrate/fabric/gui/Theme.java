package net.bummy1337.daintegrate.fabric.gui;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public class Theme {
    public static final int
            BG_DARK = 0xF0111118,
            BG_MAIN = 0xF01A1B26,
            BG_PANEL = 0xF0242538,
            BG_PANEL_HOVER = 0xF0303152,
            BG_ENTRY = 0xF02E2F44,
            BG_ENTRY_HOVER = 0xF0383958,
            BG_INPUT = 0xF01E1F2E,
            BG_INPUT_FOCUS = 0xF0252638,
            BORDER = 0xFF3A3B5E,
            BORDER_HOVER = 0xFF4D4E72,
            BORDER_FOCUS = 0xFF5865F2,
            ACCENT = 0xFF5865F2,
            ACCENT_HOVER = 0xFF4752C4,
            ACCENT_TRANSPARENT = 0x605865F2,
            TEXT_PRIMARY = 0xFFDCDDDE,
            TEXT_SECONDARY = 0xFFB9BBBE,
            TEXT_MUTED = 0xFF72767D,
            TEXT_ACCENT = 0xFFA4AEF0,
            GREEN = 0xFF3BA55C,
            GREEN_TRANSPARENT = 0x603BA55C,
            RED = 0xFFED4245,
            RED_TRANSPARENT = 0x60ED4245,
            YELLOW = 0xFFFAA61A,
            WHITE = 0xFFFFFFFF,
            BLACK = 0xFF000000,
            DIVIDER = 0xFF2A2B3D,
            SCROLLBAR = 0x805865F2,
            SCROLLBAR_HOVER = 0xFF5865F2;

    public static final int TITLE_BAR_H = 28;
    public static final int TAB_BAR_H = 30;
    public static final int STATUS_BAR_H = 22;
    public static final int PADDING = 8;
    public static final int CARD_RADIUS = 0;
    public static final int SCROLLBAR_WIDTH = 5;

    public static void fillPanel(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, BG_PANEL);
        g.outline(x, y, w, h, BORDER);
    }

    public static void fillPanelGradient(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fillGradient(x, y, x + w, y + h, BG_PANEL, BG_MAIN);
        g.outline(x, y, w, h, BORDER);
    }

    public static void fillCard(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean hovered) {
        int bg = hovered ? BG_ENTRY_HOVER : BG_ENTRY;
        g.fill(x, y, x + w, y + h, bg);
        g.outline(x, y, w, h, hovered ? BORDER_HOVER : BORDER);
    }

    public static void fillCardAccent(GuiGraphicsExtractor g, int x, int y, int w, int h) {
        g.fill(x, y, x + w, y + h, ACCENT_TRANSPARENT);
        g.outline(x, y, w, h, ACCENT);
    }

    public static void fillInput(GuiGraphicsExtractor g, int x, int y, int w, int h, boolean focused) {
        g.fill(x, y, x + w, y + h, focused ? BG_INPUT_FOCUS : BG_INPUT);
        g.outline(x, y, w, h, focused ? BORDER_FOCUS : BORDER);
    }

    public static void drawSeparator(GuiGraphicsExtractor g, int x, int y, int w) {
        g.horizontalLine(x, x + w, y, DIVIDER);
    }

    public static void drawLeftAccentBar(GuiGraphicsExtractor g, int x, int y, int h, int color) {
        g.fill(x, y, x + 3, y + h, color);
    }

    public static void drawStatusDot(GuiGraphicsExtractor g, int x, int y, int color) {
        g.fill(x, y, x + 7, y + 7, color);
    }
}
