package net.bummy1337.daintegrate.fabric.gui.clickgui.impl.trigger.render;

import net.bummy1337.daintegrate.configurations.TriggerDto;
import net.bummy1337.daintegrate.fabric.gui.Theme;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.RenderHelper;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.trigger.handler.TriggerAnimationHandler;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.trigger.handler.TriggerScrollHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class SettingsPanelRenderer {

    private static final float SETTINGS_PANEL_CORNER_RADIUS = 7f;
    private static final float CORNER_INSET = 3f;
    private static final int INFO_LINE_HEIGHT = 12;
    private static final int INFO_SPACING = 4;

    private final TriggerAnimationHandler animationHandler;

    public SettingsPanelRenderer(TriggerAnimationHandler animationHandler) {
        this.animationHandler = animationHandler;
    }

    public void render(GuiGraphicsExtractor g, TriggerDto selectedTrigger,
                       float x, float y, float width, float height, float mouseX, float mouseY, float delta,
                       int guiScale, float alphaMultiplier, TriggerScrollHandler scrollHandler, TriggerAnimationHandler animHandler) {

        int panelBg = Theme.withAlpha(Theme.BG_PANEL, (int) (15 * alphaMultiplier));
        int panelOutline = Theme.withAlpha(Theme.BORDER, (int) (215 * alphaMultiplier));
        RenderHelper.roundedRect(g, x, y, width, height, 8f, panelBg);
        RenderHelper.roundedOutline(g, x, y, width, height, 8f, panelOutline);

        if (selectedTrigger == null) {
            String text = "Select a trigger";
            float textWidth = RenderHelper.textWidth(text, 6f);
            float centerX = x + (width - textWidth) / 2f;
            float centerY = y + (height - 6f) / 2f;
            int textColor = Theme.withAlpha(Theme.TEXT_MUTED, (int) (150 * alphaMultiplier));
            RenderHelper.text(g, text, centerX, centerY, 6f, textColor);
            return;
        }

        int titleColor = Theme.withAlpha(Theme.TEXT_PRIMARY, (int) (200 * alphaMultiplier));
        RenderHelper.text(g, selectedTrigger.name, x + 8, y + 8, 7, titleColor);

        if (selectedTrigger.description != null && !selectedTrigger.description.isEmpty()) {
            String desc = selectedTrigger.description;
            int descColor = Theme.withAlpha(Theme.TEXT_MUTED, (int) (150 * alphaMultiplier));
            RenderHelper.text(g, desc.length() > 52 ? desc.substring(0, 55) + "..." : desc, x + 15, y + 20, 5, descColor);
            RenderHelper.text(g, "\u25BC", x + 8, y + 20, 6, descColor);
        }

        int dividerColor = Theme.withAlpha(Theme.DIVIDER, (int) (64 * alphaMultiplier));
        RenderHelper.rect(g, x + 8, y + 30, width - 16, 1.25f, dividerColor);

        float sideInset = CORNER_INSET;
        float bottomInset = CORNER_INSET + 3;

        float clipY = y + 31;
        float clipH = height - 26 - bottomInset;

        float clipX = x + sideInset;
        float clipW = width - sideInset * 2;

        RenderHelper.enableScissor(g, clipX, clipY, clipW, clipH);

        float infoY = y + 38f + (float) scrollHandler.getSettingDisplayScroll();

        int statusColor = selectedTrigger.isActive
                ? Theme.withAlpha(Theme.GREEN, (int) (200 * alphaMultiplier))
                : Theme.withAlpha(Theme.RED, (int) (200 * alphaMultiplier));
        String statusText = selectedTrigger.isActive ? "Active" : "Inactive";

        RenderHelper.text(g, "Status:", x + 12, infoY, 5, Theme.withAlpha(Theme.TEXT_MUTED, (int) (150 * alphaMultiplier)));
        RenderHelper.text(g, statusText, x + 42, infoY, 5, statusColor);
        infoY += INFO_LINE_HEIGHT + INFO_SPACING;

        if (selectedTrigger.sensitives != null && selectedTrigger.sensitives.length > 0) {
            RenderHelper.text(g, "Sensitives:", x + 12, infoY, 5, Theme.withAlpha(Theme.TEXT_MUTED, (int) (150 * alphaMultiplier)));
            infoY += INFO_LINE_HEIGHT;
            for (int i = 0; i < selectedTrigger.sensitives.length && i < 3; i++) {
                var sensitive = selectedTrigger.sensitives[i];
                if (sensitive != null && sensitive.toString() != null) {
                    String label = sensitive.toString();
                    if (label.length() > 30) label = label.substring(0, 30) + "...";
                    RenderHelper.text(g, "  \u2022 " + label, x + 16, infoY, 5,
                            Theme.withAlpha(Theme.TEXT_SECONDARY, (int) (130 * alphaMultiplier)));
                    infoY += INFO_LINE_HEIGHT;
                }
            }
        }

        if (selectedTrigger.handlers != null && selectedTrigger.handlers.length > 0) {
            RenderHelper.text(g, "Handlers:", x + 12, infoY, 5, Theme.withAlpha(Theme.TEXT_MUTED, (int) (150 * alphaMultiplier)));
            infoY += INFO_LINE_HEIGHT;
            for (int i = 0; i < selectedTrigger.handlers.length && i < 3; i++) {
                var handler = selectedTrigger.handlers[i];
                if (handler != null && handler.toString() != null) {
                    String label = handler.toString();
                    if (label.length() > 30) label = label.substring(0, 30) + "...";
                    RenderHelper.text(g, "  \u2022 " + label, x + 16, infoY, 5,
                            Theme.withAlpha(Theme.TEXT_SECONDARY, (int) (130 * alphaMultiplier)));
                    infoY += INFO_LINE_HEIGHT;
                }
            }
        }

        RenderHelper.disableScissor(g);

        renderScrollFade(g, x + sideInset, clipY, width - sideInset * 2, clipH,
                scrollHandler.getSettingScrollTopFade() * alphaMultiplier,
                scrollHandler.getSettingScrollBottomFade() * alphaMultiplier, 60, 12);
    }

    public float calculateTotalHeight(TriggerDto selectedTrigger) {
        if (selectedTrigger == null) return 0;

        float total = 40f;

        total += INFO_LINE_HEIGHT + INFO_SPACING;

        if (selectedTrigger.sensitives != null && selectedTrigger.sensitives.length > 0) {
            total += INFO_LINE_HEIGHT;
            total += Math.min(selectedTrigger.sensitives.length, 3) * INFO_LINE_HEIGHT;
        }

        if (selectedTrigger.handlers != null && selectedTrigger.handlers.length > 0) {
            total += INFO_LINE_HEIGHT;
            total += Math.min(selectedTrigger.handlers.length, 3) * INFO_LINE_HEIGHT;
        }

        return total;
    }

    private void renderScrollFade(GuiGraphicsExtractor g, float x, float y, float w, float h, float topFade, float bottomFade, int alpha, int size) {
        if (topFade > 0.01f) {
            for (int i = 0; i < size; i++) {
                float fadeAlpha = alpha * topFade * (1f - i / (float) size);
                int fadeColor = Theme.withAlpha(Theme.BG_DARK, (int) fadeAlpha);
                RenderHelper.rect(g, x, y + i, w, 1, fadeColor);
            }
        }
        if (bottomFade > 0.01f) {
            for (int i = 0; i < size; i++) {
                float fadeAlpha = alpha * bottomFade * (i / (float) size);
                int fadeColor = Theme.withAlpha(Theme.BG_DARK, (int) fadeAlpha);
                RenderHelper.rect(g, x, y + h - size + i, w, 1, fadeColor);
            }
        }
    }
}
