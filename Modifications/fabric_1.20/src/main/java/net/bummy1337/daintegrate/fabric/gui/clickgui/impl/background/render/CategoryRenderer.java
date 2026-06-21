package net.bummy1337.daintegrate.fabric.gui.clickgui.impl.background.render;

import net.bummy1337.daintegrate.fabric.gui.Theme;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.RenderHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.HashMap;
import java.util.Map;

public class CategoryRenderer {

    private static final String[] MAIN_CATEGORIES = {"Dashboard", "Donate", "Subscribe", "Twitch", "Connection", "Help"};
    private static final String[] MAIN_CATEGORY_ICONS = {"H", "D", "S", "T", "C", "?"};
    private static final int TRIGGER_SECTION_START = 1;
    private static final int TRIGGER_SECTION_END = 3;
    private static final int EXTRA_SECTION_START = 4;

    private static final float ANIMATION_SPEED = 8f;
    private static final float MAX_OFFSET = 5f;
    private static final float BALL_SIZE = 3f;
    private static final float TEXT_SIZE = 6f;
    private static final float ICON_SIZE = 6f;
    private static final float ICON_SPACING = 4f;
    private static final float SECTION_TEXT_SIZE = 5f;

    private final Map<String, Float> categoryAnimations = new HashMap<>();

    public CategoryRenderer() {
        for (String cat : MAIN_CATEGORIES) {
            categoryAnimations.put(cat, 0f);
        }
    }

    public void updateAnimations(String selectedCategory, float deltaTime) {
        for (String cat : MAIN_CATEGORIES) {
            updateCategoryAnimation(cat, selectedCategory, deltaTime);
        }
    }

    private void updateCategoryAnimation(String cat, String selected, float deltaTime) {
        float target = cat.equals(selected) ? 1f : 0f;
        float current = categoryAnimations.getOrDefault(cat, 0f);

        float diff = target - current;
        float change = diff * ANIMATION_SPEED * deltaTime;

        if (Math.abs(diff) < 0.001f) {
            categoryAnimations.put(cat, target);
        } else {
            categoryAnimations.put(cat, current + change);
        }
    }

    public void render(GuiGraphicsExtractor g, float bgX, float bgY, String selectedCategory, float alphaMultiplier) {
        renderSingleTopItem(g, bgX, bgY, alphaMultiplier);
        renderSectionHeader(g, bgX, bgY + 70f, "Categories", alphaMultiplier);
        renderMainCategories(g, bgX, bgY, alphaMultiplier);
        renderSectionHeader(g, bgX, bgY + 139f, "Other", alphaMultiplier);
        renderExtraCategories(g, bgX, bgY, alphaMultiplier);
    }

    private void renderSectionHeader(GuiGraphicsExtractor g, float bgX, float sectionY, String title, float alphaMultiplier) {
        float lineWidth = 14f;
        float textWidth = RenderHelper.textWidth(title, SECTION_TEXT_SIZE);
        float totalWidth = 64f;
        float textX = bgX + 15f + (totalWidth - textWidth) / 2f;
        float lineY = sectionY + 8f;

        int lineAlpha = (int) (40 * alphaMultiplier);
        int textAlpha = (int) (100 * alphaMultiplier);

        RenderHelper.rect(g, bgX + 15f, lineY, lineWidth, 0.5f, Theme.withAlpha(Theme.WHITE, lineAlpha));
        RenderHelper.rect(g, bgX + 15f + totalWidth - lineWidth, lineY, lineWidth, 0.5f, Theme.withAlpha(Theme.WHITE, lineAlpha));
        RenderHelper.text(g, title, textX, sectionY - 1f, SECTION_TEXT_SIZE, Theme.withAlpha(Theme.TEXT_MUTED, textAlpha));
    }

    private void renderSingleTopItem(GuiGraphicsExtractor g, float bgX, float bgY, float alphaMultiplier) {
        float animation = categoryAnimations.getOrDefault(MAIN_CATEGORIES[0], 0f);
        renderCategoryItem(g, bgX, bgY + 52f, MAIN_CATEGORIES[0], MAIN_CATEGORY_ICONS[0], animation, alphaMultiplier);
    }

    private void renderMainCategories(GuiGraphicsExtractor g, float bgX, float bgY, float alphaMultiplier) {
        for (int i = TRIGGER_SECTION_START; i <= TRIGGER_SECTION_END; i++) {
            String cat = MAIN_CATEGORIES[i];
            float animation = categoryAnimations.getOrDefault(cat, 0f);
            float textY = bgY + 86f + (i - TRIGGER_SECTION_START) * 16f;
            renderCategoryItem(g, bgX, textY, MAIN_CATEGORIES[i], MAIN_CATEGORY_ICONS[i], animation, alphaMultiplier);
        }
    }

    private void renderExtraCategories(GuiGraphicsExtractor g, float bgX, float bgY, float alphaMultiplier) {
        for (int i = EXTRA_SECTION_START; i < MAIN_CATEGORIES.length; i++) {
            String cat = MAIN_CATEGORIES[i];
            float animation = categoryAnimations.getOrDefault(cat, 0f);
            float textY = bgY + 155f + (i - EXTRA_SECTION_START) * 16f;
            renderCategoryItem(g, bgX, textY, MAIN_CATEGORIES[i], MAIN_CATEGORY_ICONS[i], animation, alphaMultiplier);
        }
    }

    private void renderCategoryItem(GuiGraphicsExtractor g, float bgX, float textY, String name, String icon,
                                    float animation, float alphaMultiplier) {
        float offsetX = animation * MAX_OFFSET;

        int baseGray = 128;
        int targetWhite = 255;
        int colorValue = (int) (baseGray + (targetWhite - baseGray) * animation);
        int alpha = (int) ((128 + 127 * animation) * alphaMultiplier);
        int textColor = Theme.withAlpha(Theme.TEXT_PRIMARY, alpha);

        float iconX = bgX + 17f + offsetX;
        float iconWidth = RenderHelper.textWidth(icon, ICON_SIZE);
        float textX = iconX + iconWidth + ICON_SPACING;
        float textWidth = RenderHelper.textWidth(name, TEXT_SIZE);

        RenderHelper.text(g, icon, iconX, textY + 0.5f, ICON_SIZE, textColor);

        if (animation > 0.01f) {
            float lineWidth = (iconWidth + ICON_SPACING + textWidth) * animation;
            float lineAlpha = animation * 60 * alphaMultiplier;
            RenderHelper.rect(g, iconX, textY + 9f, lineWidth, 0.5f, Theme.withAlpha(Theme.WHITE, (int) lineAlpha));

            float ballAlpha = animation * 200 * alphaMultiplier;
            float ballX = bgX + 12f;
            float ballY = textY + 2.5f;
            RenderHelper.roundedRect(g, ballX, ballY, BALL_SIZE, BALL_SIZE, 2f, Theme.withAlpha(Theme.ACCENT, (int) ballAlpha));
        }

        RenderHelper.text(g, name, textX, textY, TEXT_SIZE, textColor);
    }

    public String getCategoryAtPosition(double mouseX, double mouseY, float bgX, float bgY) {
        if (mouseX < bgX + 10f || mouseX > bgX + 95f) return null;

        for (int i = 0; i < MAIN_CATEGORIES.length; i++) {
            float catY;
            if (i == 0) catY = 52f;
            else if (i <= TRIGGER_SECTION_END) catY = 86f + (i - TRIGGER_SECTION_START) * 16f;
            else catY = 155f + (i - EXTRA_SECTION_START) * 16f;
            if (mouseY >= bgY + catY && mouseY <= bgY + catY + 14f) {
                return MAIN_CATEGORIES[i];
            }
        }

        return null;
    }
}
