package net.bummy1337.daintegrate.fabric.gui.clickgui.impl;

import net.bummy1337.daintegrate.fabric.gui.FontHelper;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class RenderHelper {
    private static Font font;

    public static void setFont(Font f) {
        font = f;
    }

    public static Font getFont() {
        return font;
    }

    public static void rect(GuiGraphicsExtractor g, float x, float y, float w, float h, int color) {
        g.fill((int) x, (int) y, (int) (x + w), (int) (y + h), color);
    }

    public static void roundedRect(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, int color) {
        int r = Math.max(0, Math.min((int) radius, (int) Math.min(w, h) / 2));
        if (r <= 1) {
            rect(g, x, y, w, h, color);
            return;
        }

        g.fill((int) (x + r), (int) y, (int) (x + w - r), (int) (y + h), color);
        g.fill((int) x, (int) (y + r), (int) (x + w), (int) (y + h - r), color);

        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int dx = (int) Math.ceil(r - Math.sqrt(Math.max(0, r * r - dy * dy)));
            int topY = (int) y + i;
            int bottomY = (int) (y + h) - i - 1;
            g.fill((int) x + dx, topY, (int) (x + w) - dx, topY + 1, color);
            g.fill((int) x + dx, bottomY, (int) (x + w) - dx, bottomY + 1, color);
        }
    }

    public static void outline(GuiGraphicsExtractor g, float x, float y, float w, float h, float stroke, int color) {
        g.outline((int) x, (int) y, (int) w, (int) h, color);
    }

    public static void roundedOutline(GuiGraphicsExtractor g, float x, float y, float w, float h, float radius, int color) {
        g.outline((int) x, (int) y, (int) w, (int) h, color);
    }

    public static void gradientRect(GuiGraphicsExtractor g, float x, float y, float w, float h, int[] colors) {
        int tl = colors.length > 0 ? colors[0] : 0;
        int tr = colors.length > 1 ? colors[1] : tl;
        int bl = colors.length > 2 ? colors[2] : tl;
        int br = colors.length > 3 ? colors[3] : bl;
        g.fillGradient((int) x, (int) y, (int) (x + w), (int) (y + h), tl, br);
    }

    public static void text(GuiGraphicsExtractor g, String text, float x, float y, float size, int color) {
        if (font == null) return;
        g.text(font, FontHelper.comp(text), (int) x, (int) y, color, false);
    }

    public static void textCentered(GuiGraphicsExtractor g, String text, float centerX, float y, float size, int color) {
        if (font == null) return;
        int w = FontHelper.width(font, text);
        g.text(font, FontHelper.comp(text), (int) (centerX - w / 2f), (int) y, color, false);
    }

    public static float textWidth(String text, float size) {
        if (font == null) return 0;
        return FontHelper.width(font, text);
    }

    public static void enableScissor(GuiGraphicsExtractor g, float x, float y, float w, float h) {
        g.enableScissor((int) x, (int) y, (int) (x + w), (int) (y + h));
    }

    public static void disableScissor(GuiGraphicsExtractor g) {
        g.disableScissor();
    }
}
