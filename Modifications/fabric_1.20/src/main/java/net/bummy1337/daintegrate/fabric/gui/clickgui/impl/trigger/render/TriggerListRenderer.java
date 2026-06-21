package net.bummy1337.daintegrate.fabric.gui.clickgui.impl.trigger.render;

import net.bummy1337.daintegrate.configurations.TriggerDto;
import net.bummy1337.daintegrate.fabric.gui.Theme;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.RenderHelper;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.trigger.handler.TriggerAnimationHandler;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.trigger.handler.TriggerScrollHandler;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.trigger.util.TriggerDisplayHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;
import java.util.Map;

public class TriggerListRenderer {

    private static final float TRIGGER_ITEM_HEIGHT = 22f;
    private static final float TRIGGER_LIST_CORNER_RADIUS = 6f;
    private static final float CORNER_INSET = 3f;
    private static final float STATE_BALL_SIZE = 3f;
    private static final float STATE_TEXT_OFFSET = 6f;

    private final TriggerAnimationHandler animationHandler;
    private final TriggerDisplayHelper displayHelper;

    public TriggerListRenderer(TriggerAnimationHandler animationHandler, TriggerDisplayHelper displayHelper) {
        this.animationHandler = animationHandler;
        this.displayHelper = displayHelper;
    }

    public void render(GuiGraphicsExtractor g, List<TriggerDto> displayTriggers, TriggerDto selectedTrigger,
                       float x, float y, float width, float height,
                       float mouseX, float mouseY, int guiScale, float alphaMultiplier,
                       TriggerAnimationHandler animHandler, TriggerScrollHandler scrollHandler) {

        int panelBg = Theme.withAlpha(Theme.BG_PANEL, (int) (15 * alphaMultiplier));
        int panelOutline = Theme.withAlpha(Theme.BORDER, (int) (215 * alphaMultiplier));
        RenderHelper.roundedRect(g, x, y, width, height, 8f, panelBg);
        RenderHelper.roundedOutline(g, x, y, width, height, 8f, panelOutline);

        float topInset = CORNER_INSET;
        float bottomInset = CORNER_INSET;
        float sideInset = CORNER_INSET;

        RenderHelper.enableScissor(g, x + sideInset, y + topInset - 1.5f, width - sideInset * 2, height - topInset - bottomInset + 3);

        if (animHandler.isCategoryTransitioning() && !animHandler.getOldTriggers().isEmpty()) {
            float oldAlpha = (1f - animHandler.getCategoryTransitionProgress()) * alphaMultiplier;
            float oldOffsetX = animHandler.easeInCubic(animHandler.getCategoryTransitionProgress()) * -animHandler.getCategorySlideDistance();
            float oldScale = 1f - animHandler.getCategoryTransitionProgress() * 0.1f;

            renderTriggerItems(g, animHandler.getOldTriggers(), animHandler.getOldTriggerAnimations(),
                    selectedTrigger, x, y, width, height, mouseX, mouseY,
                    oldAlpha, oldOffsetX, oldScale, (float) animHandler.getOldTriggerDisplayScroll(), false, topInset, bottomInset, animHandler);
        }

        float newAlpha;
        float newOffsetX;
        float newScale;

        if (animHandler.isCategoryTransitioning()) {
            float entryProgress = Math.max(0f, (animHandler.getCategoryTransitionProgress() - 0.2f) / 0.8f);
            entryProgress = animHandler.easeOutQuart(entryProgress);
            newAlpha = entryProgress * alphaMultiplier;
            newOffsetX = (1f - entryProgress) * animHandler.getCategorySlideDistance();
            newScale = 0.9f + entryProgress * 0.1f;
        } else {
            newAlpha = alphaMultiplier;
            newOffsetX = 0f;
            newScale = 1f;
        }

        renderTriggerItems(g, displayTriggers, animHandler.getTriggerAnimations(),
                selectedTrigger, x, y, width, height, mouseX, mouseY,
                newAlpha, newOffsetX, newScale, (float) scrollHandler.getTriggerDisplayScroll(), true, topInset, bottomInset, animHandler);

        RenderHelper.disableScissor(g);

        renderScrollFade(g, x, y + topInset, width, height - topInset - bottomInset,
                scrollHandler.getTriggerScrollTopFade() * alphaMultiplier,
                scrollHandler.getTriggerScrollBottomFade() * alphaMultiplier, 80, 15);
    }

    private void renderTriggerItems(GuiGraphicsExtractor g, List<TriggerDto> triggerList, Map<TriggerDto, Float> animations,
                                    TriggerDto selectedTrigger,
                                    float x, float y, float width, float height, float mouseX, float mouseY,
                                    float alphaMultiplier, float offsetX, float scale, float scrollOffset,
                                    boolean interactive, float topInset, float bottomInset, TriggerAnimationHandler animHandler) {
        if (alphaMultiplier <= 0.01f) return;

        float startY = y + topInset + 2f + scrollOffset;
        float centerY = y + height / 2f;
        float visibleTop = y + topInset;
        float visibleBottom = y + height - bottomInset;

        for (int i = 0; i < triggerList.size(); i++) {
            TriggerDto trigger = triggerList.get(i);
            float triggerY = startY + i * (TRIGGER_ITEM_HEIGHT + 2);

            if (triggerY + TRIGGER_ITEM_HEIGHT < visibleTop || triggerY > visibleBottom) continue;

            float itemProgress = animations.getOrDefault(trigger, 1f);
            float posAnim = animHandler.getPositionAnimations().getOrDefault(trigger, 1f);
            float alphaAnim = animHandler.getTriggerAlphaAnimations().getOrDefault(trigger, 1f);
            float combinedAlpha = itemProgress * alphaMultiplier * alphaAnim;

            if (combinedAlpha <= 0.01f) continue;

            float itemAnimOffset = (1f - itemProgress) * 20f;
            float posAnimOffset = (1f - easeOutCubic(posAnim)) * 15f;

            float scaledTriggerY = centerY + (triggerY - centerY) * scale;
            float scaledHeight = TRIGGER_ITEM_HEIGHT * scale;

            float animX = x + 3 + offsetX + itemAnimOffset + posAnimOffset;

            boolean selected = interactive && trigger == selectedTrigger;
            boolean isHighlighted = interactive && trigger == animHandler.getHighlightedTrigger() && animHandler.getHighlightAnimation() > 0.01f;
            float hoverAnim = interactive ? animHandler.getHoverAnimations().getOrDefault(trigger, 0f) : 0f;
            float stateAnim = interactive ? animHandler.getStateAnimations().getOrDefault(trigger, trigger.isActive ? 1f : 0f) : (trigger.isActive ? 1f : 0f);
            float selectedIconAnim = interactive ? animHandler.getSelectedIconAnimations().getOrDefault(trigger, 0f) : 0f;

            int bg;
            if (selected) {
                int bgAlpha = (int) ((55 + hoverAnim * 10) * combinedAlpha);
                bg = Theme.withAlpha(Theme.BG_ENTRY_HOVER, bgAlpha);
            } else {
                int bgAlpha = (int) ((25 + (45 - 25) * hoverAnim) * combinedAlpha);
                int gray = (int) (64 + 36 * hoverAnim);
                int bgArgb = 0xFF000000 | (gray << 16) | (gray << 8) | gray;
                bg = (bgArgb & 0x00FFFFFF) | ((bgAlpha & 0xFF) << 24);
            }

            float scaledWidth = (width - 6) * scale;

            RenderHelper.roundedRect(g, animX, scaledTriggerY, scaledWidth, scaledHeight, 4f, bg);

            if (selected) {
                float pulseValue = (float) (Math.sin(animHandler.getSelectedPulseAnimation()) * 0.5 + 0.5);
                float highlightBoost = isHighlighted ? animHandler.getHighlightAnimation() * 0.5f : 0f;
                int outlineAlpha = (int) ((80 + 40 * pulseValue + 80 * highlightBoost) * combinedAlpha);
                int r = (int) Math.min(255, 80 + 50 * highlightBoost + 30 * pulseValue);
                int gVal = (int) Math.min(255, 80 + 20 * pulseValue + 40 * highlightBoost);
                int b = (int) Math.min(255, 80 + 20 * pulseValue + 40 * highlightBoost);
                int outlineArgb = 0xFF000000 | (r << 16) | (gVal << 8) | b;
                int outlineColor = (outlineArgb & 0x00FFFFFF) | ((outlineAlpha & 0xFF) << 24);
                RenderHelper.outline(g, animX, scaledTriggerY, scaledWidth, scaledHeight, 0.5f, outlineColor);
            } else if (hoverAnim > 0.01f) {
                int outlineAlpha = (int) (60 * hoverAnim * combinedAlpha);
                int outlineColor = Theme.withAlpha(Theme.TEXT_SECONDARY, outlineAlpha);
                RenderHelper.outline(g, animX, scaledTriggerY, scaledWidth, scaledHeight, 0.5f, outlineColor);
            }

            float stateTextOffset = stateAnim * STATE_TEXT_OFFSET;

            if (stateAnim > 0.01f) {
                int ballAlpha = (int) (stateAnim * 200 * combinedAlpha);
                float ballX = animX + 4;
                float ballY = scaledTriggerY + (scaledHeight - STATE_BALL_SIZE * scale) / 2f + 1F;
                int ballColor = Theme.withAlpha(Theme.ACCENT, ballAlpha);
                RenderHelper.roundedRect(g, ballX, ballY, STATE_BALL_SIZE * scale, STATE_BALL_SIZE * scale, 2f, ballColor);
            }

            String name = trigger.name;

            int baseGray = 128;
            int targetWhite = 255;
            int textBrightness = (int) (baseGray + (targetWhite - baseGray) * stateAnim);
            int textAlphaValue = (int) ((180 + 75 * stateAnim) * combinedAlpha);

            if (hoverAnim > 0.01f && stateAnim < 0.99f) {
                textBrightness = (int) (textBrightness + (40 * hoverAnim * (1 - stateAnim)));
                textAlphaValue = (int) (textAlphaValue + (40 * hoverAnim * (1 - stateAnim)));
            }

            if (isHighlighted) {
                textBrightness = (int) Math.min(255, textBrightness + 30 * animHandler.getHighlightAnimation());
            }

            int textColorArgb = 0xFF000000 | (textBrightness << 16) | (textBrightness << 8) | textBrightness;
            int textColor = (textColorArgb & 0x00FFFFFF) | ((Math.min(255, textAlphaValue) & 0xFF) << 24);

            float textX = animX + 5 + stateTextOffset;
            float textY = scaledTriggerY + (scaledHeight - 6f * scale) / 2f;
            RenderHelper.text(g, name, textX, textY, 6 * scale, textColor);

            if (interactive) {
                if (selectedIconAnim > 0.01f) {
                    float gearAlpha = 150 * selectedIconAnim * combinedAlpha;
                    int gearColor = Theme.withAlpha(Theme.TEXT_SECONDARY, (int) gearAlpha);
                    RenderHelper.text(g, "\u2699", animX + scaledWidth - 14, scaledTriggerY + (scaledHeight - 8f * scale) / 2f + 1, 8 * scale, gearColor);
                } else {
                    float dotsAlpha = 120 * (1f - selectedIconAnim) * combinedAlpha;
                    int dotsColor = Theme.withAlpha(Theme.TEXT_MUTED, (int) dotsAlpha);
                    RenderHelper.text(g, "...", animX + scaledWidth - 14 + 1f, scaledTriggerY + (scaledHeight - 8f * scale) / 2f - 1f, 7 * scale, dotsColor);
                }
            }
        }
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

    public TriggerDto getTriggerAtPosition(List<TriggerDto> displayTriggers, double mouseX, double mouseY,
                                           float listX, float listY, float listWidth, float listHeight,
                                           double scrollOffset, boolean isTransitioning) {
        if (isTransitioning) return null;
        if (mouseX < listX || mouseX > listX + listWidth || mouseY < listY || mouseY > listY + listHeight) return null;

        float startY = listY + CORNER_INSET + 2f + (float) scrollOffset;
        for (int i = 0; i < displayTriggers.size(); i++) {
            float triggerY = startY + i * (TRIGGER_ITEM_HEIGHT + 2);
            if (mouseX >= listX + 3 && mouseX <= listX + listWidth - 3 && mouseY >= triggerY && mouseY <= triggerY + TRIGGER_ITEM_HEIGHT) {
                return displayTriggers.get(i);
            }
        }
        return null;
    }

    private float easeOutCubic(float x) {
        return 1f - (float) Math.pow(1 - x, 3);
    }
}
