package net.bummy1337.daintegrate.fabric.gui.clickgui.impl.background.render;

import net.bummy1337.daintegrate.fabric.gui.Theme;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.RenderHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class BackgroundRenderer {
    public void render(GuiGraphicsExtractor g, float bgX, float bgY, float bgWidth, float bgHeight, float alphaMultiplier) {
        int tl = Theme.withAlpha(Theme.BG_MAIN, alphaMultiplier);
        int br = Theme.withAlpha(Theme.BG_DARK, alphaMultiplier);
        RenderHelper.roundedRect(g, bgX, bgY, bgWidth, bgHeight, 10f, tl);
        g.fillGradient((int) (bgX + 2), (int) (bgY + 2), (int) (bgX + bgWidth - 2), (int) (bgY + bgHeight - 2), tl, br);
        RenderHelper.roundedOutline(g, bgX, bgY, bgWidth, bgHeight, 10f, Theme.withAlpha(Theme.BORDER, alphaMultiplier));
    }

    public void renderCategoryPanel(GuiGraphicsExtractor g, float bgX, float bgY, float bgHeight, float alphaMultiplier) {
        float x = bgX + 7.5f;
        float y = bgY + 7.5f;
        float w = 80;
        float h = bgHeight - 15;

        RenderHelper.roundedRect(g, x, y, w, h, 8f, Theme.withAlpha(Theme.BG_PANEL, alphaMultiplier));
        RenderHelper.roundedOutline(g, x, y, w, h, 8f, Theme.withAlpha(Theme.BORDER, alphaMultiplier));

        RenderHelper.roundedRect(g, bgX + 12.5f, bgY + bgHeight - 29.5f, 70, 17, 5f, Theme.withAlpha(Theme.BG_INPUT, alphaMultiplier));
        RenderHelper.roundedOutline(g, bgX + 12.5f, bgY + bgHeight - 29.5f, 70, 17, 5f, Theme.withAlpha(Theme.BORDER, alphaMultiplier));

        float textWidth = RenderHelper.textWidth("Coming Soon...", 6f);
        float centerX = bgX + 12.5f + (70 - textWidth) / 2f;
        float centerY = bgY + bgHeight - 29.5f + (17 - 8) / 2f;
        RenderHelper.text(g, "Coming Soon...", centerX, centerY, 6f, Theme.withAlpha(Theme.TEXT_MUTED, alphaMultiplier));
    }
}
