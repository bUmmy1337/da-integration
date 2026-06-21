package net.bummy1337.daintegrate.fabric.gui.clickgui.impl.background.render;

import net.bummy1337.daintegrate.fabric.gui.Theme;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.RenderHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class AvatarRenderer {

    private static final String MOD_NAME = "DA INTEGRATE";
    private static final String MOD_VERSION = "1.2";

    public void render(GuiGraphicsExtractor g, float bgX, float bgY, float alphaMultiplier) {
        int alpha = (int) (255 * alphaMultiplier);
        int alphaBg = (int) (105 * alphaMultiplier);
        int alphaText = (int) (200 * alphaMultiplier);

        RenderHelper.roundedRect(g, bgX + 12.5f, bgY + 12.5f, 70, 30, 7f, Theme.withAlpha(Theme.BG_ENTRY, alpha));

        RenderHelper.roundedRect(g, bgX + 15f, bgY + 15f, 25, 25, 6f, Theme.withAlpha(Theme.BG_PANEL, alpha));
        RenderHelper.textCentered(g, "DA", bgX + 27.5f, bgY + 23f, 8, Theme.withAlpha(Theme.ACCENT, alphaText));

        RenderHelper.roundedRect(g, bgX + 12.5f, bgY + 12.5f, 70, 30, 7f, Theme.withAlpha(Theme.BLACK, alphaBg));

        float textX = bgX + 44;
        float textY = bgY + 18;
        float maxTextWidth = 35f;

        RenderHelper.enableScissor(g, textX, textY - 2, maxTextWidth, 14f);
        RenderHelper.text(g, MOD_NAME, textX, textY, 6, Theme.withAlpha(Theme.TEXT_PRIMARY, alphaText));
        RenderHelper.text(g, "v" + MOD_VERSION, textX, textY + 7, 5, Theme.withAlpha(Theme.TEXT_MUTED, alphaText));
        RenderHelper.disableScissor(g);
    }
}
