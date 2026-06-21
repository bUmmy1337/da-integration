package net.bummy1337.daintegrate.fabric.gui.clickgui.impl.settingsrender;

import net.bummy1337.daintegrate.fabric.gui.FontHelper;
import net.bummy1337.daintegrate.fabric.gui.Theme;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SelectComponent extends AbstractSettingComponent {
    private final String settingName;
    private final String settingDescription;
    private final List<String> options;
    private final java.util.function.Supplier<String> getter;
    private final java.util.function.Consumer<String> setter;

    private boolean expanded = false;
    private float expandAnimation = 0f;
    private float hoverAnimation = 0f;

    private float descScrollOffset = 0f;
    private boolean descScrollingRight = true;
    private long descScrollPauseTime = 0;

    private float arrowRotation = 0f;

    private final Map<String, Float> optionHoverAnimations = new HashMap<>();
    private final Map<String, Float> selectAnimations = new HashMap<>();

    private String previousSelected = "";
    private float selectedTextAlpha = 1f;
    private float selectedTextSlide = 1f;
    private float newSelectedTextAlpha = 0f;
    private float newSelectedTextSlide = 0f;
    private String animatingFromText = "";
    private boolean isAnimatingSelection = false;

    private long lastUpdateTime = System.currentTimeMillis();

    private static final float ANIMATION_SPEED = 8f;
    private static final float COLLAPSE_SPEED = 15f;
    private static final float BOX_WIDTH = 65f;
    private static final float OPTION_HEIGHT = 14f;
    private static final long SCROLL_PAUSE_DURATION = 2000;
    private static final float SCROLL_PIXELS_PER_SECOND = 20f;
    private static final float DESC_PADDING = 8f;
    private static final float SELECTION_ANIMATION_SPEED = 10f;

    public SelectComponent(String name, String description, List<String> options,
                           java.util.function.Supplier<String> getter,
                           java.util.function.Consumer<String> setter) {
        super(name, description);
        this.settingName = name;
        this.settingDescription = description;
        this.options = options;
        this.getter = getter;
        this.setter = setter;
        this.previousSelected = getter.get() != null ? getter.get() : (options.isEmpty() ? "" : options.get(0));
        for (String option : options) {
            optionHoverAnimations.put(option, 0f);
            selectAnimations.put(option, option.equals(previousSelected) ? 1f : 0f);
        }
    }

    private float getDeltaTime() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((currentTime - lastUpdateTime) / 1000f, 0.1f);
        lastUpdateTime = currentTime;
        return deltaTime;
    }

    private float lerp(float current, float target, float speed) {
        float diff = target - current;
        if (Math.abs(diff) < 0.001f) {
            return target;
        }
        return current + diff * Math.min(speed, 1f);
    }

    private void updateSelectionAnimation(float deltaTime) {
        String currentSelected = getter.get() != null ? getter.get() : "";

        if (!currentSelected.equals(previousSelected) && !isAnimatingSelection) {
            animatingFromText = previousSelected;
            isAnimatingSelection = true;
            selectedTextAlpha = 1f;
            selectedTextSlide = 1f;
            newSelectedTextAlpha = 0f;
            newSelectedTextSlide = 0f;
        }

        if (isAnimatingSelection) {
            selectedTextAlpha = lerp(selectedTextAlpha, 0f, deltaTime * SELECTION_ANIMATION_SPEED);
            selectedTextSlide = lerp(selectedTextSlide, 0f, deltaTime * SELECTION_ANIMATION_SPEED);

            if (selectedTextAlpha < 0.5f) {
                newSelectedTextAlpha = lerp(newSelectedTextAlpha, 1f, deltaTime * SELECTION_ANIMATION_SPEED);
                newSelectedTextSlide = lerp(newSelectedTextSlide, 1f, deltaTime * SELECTION_ANIMATION_SPEED);
            }

            if (newSelectedTextAlpha > 0.99f && newSelectedTextSlide > 0.99f) {
                isAnimatingSelection = false;
                previousSelected = currentSelected;
                selectedTextAlpha = 1f;
                selectedTextSlide = 1f;
                newSelectedTextAlpha = 1f;
                newSelectedTextSlide = 1f;
            }
        } else {
            previousSelected = currentSelected;
        }
    }

    @Override
    public void render(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        float deltaTime = getDeltaTime();
        var font = Minecraft.getInstance().font;

        updateSelectionAnimation(deltaTime);

        boolean mainHovered = isMainHover(mouseX, mouseY);
        hoverAnimation = lerp(hoverAnimation, mainHovered ? 1f : 0f, deltaTime * ANIMATION_SPEED);

        float expandSpeed = expanded ? ANIMATION_SPEED : COLLAPSE_SPEED;
        expandAnimation = lerp(expandAnimation, expanded ? 1f : 0f, deltaTime * expandSpeed);

        float targetRotation = expanded ? 90f : 0f;
        arrowRotation = lerp(arrowRotation, targetRotation, deltaTime * ANIMATION_SPEED);

        RenderHelper.text(g, settingName, x + 9.5f, y + height / 2 - 7.5f, 6,
                Theme.withAlpha(Theme.TEXT_PRIMARY, (int)(200 * alphaMultiplier)));

        if (settingDescription != null && !settingDescription.isEmpty()) {
            renderScrollingDescription(g, font, settingDescription, deltaTime);
        }

        float boxX = x + width - BOX_WIDTH - 2;
        float boxY = y + height / 2 - 5;
        float boxHeight = 10f;

        int bgAlpha = 25 + (int)(hoverAnimation * 15);
        RenderHelper.rect(g, boxX, boxY, BOX_WIDTH, boxHeight,
                Theme.withAlpha(Theme.BG_INPUT, (int)(bgAlpha * alphaMultiplier)));

        int outlineAlpha = 60 + (int)(hoverAnimation * 40);
        RenderHelper.outline(g, boxX, boxY, BOX_WIDTH, boxHeight, 0.5f,
                Theme.withAlpha(Theme.BORDER, (int)(outlineAlpha * alphaMultiplier)));

        renderAnimatedSelectedText(g, font, boxX, boxY, boxHeight);

        renderArrowIcon(g, boxX + BOX_WIDTH - 8, boxY + boxHeight / 2 - 4f);

        if (expandAnimation > 0.01f) {
            renderExpandedOptions(g, font, mouseX, mouseY, boxX, boxY + boxHeight + 2, deltaTime);
        }
    }

    private void renderArrowIcon(GuiGraphicsExtractor g, float iconX, float iconY) {
        int arrowAlpha = 120 + (int)(hoverAnimation * 60);
        float rad = (float) Math.toRadians(arrowRotation);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

        float centerX = iconX + 4f;
        float centerY = iconY + 4f;
        float offsetX = -4f;
        float offsetY = -4f;

        float rotatedX = centerX + (offsetX * cos - offsetY * sin);
        float rotatedY = centerY + (offsetX * sin + offsetY * cos);

        float triSize = 3f;
        int triColor = Theme.withAlpha(Theme.TEXT_SECONDARY, (int)(arrowAlpha * alphaMultiplier));
        RenderHelper.rect(g, rotatedX, rotatedY, triSize, 1f, triColor);
        RenderHelper.rect(g, rotatedX + 0.5f, rotatedY + 1f, triSize - 1f, 1f, triColor);
        RenderHelper.rect(g, rotatedX + 1f, rotatedY + 2f, triSize - 2f, 1f, triColor);
    }

    private void renderAnimatedSelectedText(GuiGraphicsExtractor g, net.minecraft.client.gui.Font font,
                                            float boxX, float boxY, float boxHeight) {
        float maxTextWidth = BOX_WIDTH - 14;
        float textY = boxY + boxHeight / 2 - 2.5f;

        RenderHelper.enableScissor(g, boxX + 2, boxY, maxTextWidth + 2, boxHeight);

        if (isAnimatingSelection) {
            if (selectedTextAlpha > 0.01f) {
                String displayOld = truncateText(font, animatingFromText, maxTextWidth);
                float slideOffset = (1f - selectedTextSlide) * -15f;
                int alpha = (int)(200 * selectedTextAlpha * alphaMultiplier);
                RenderHelper.text(g, displayOld, boxX + 4 + slideOffset, textY, 5,
                        Theme.withAlpha(Theme.TEXT_MUTED, alpha));
            }

            if (newSelectedTextAlpha > 0.01f) {
                String selected = getter.get() != null ? getter.get() : "";
                String displayNew = truncateText(font, selected, maxTextWidth);
                float slideOffset = (1f - newSelectedTextSlide) * 20f;
                int alpha = (int)(200 * newSelectedTextAlpha * alphaMultiplier);
                RenderHelper.text(g, displayNew, boxX + 4 + slideOffset, textY, 5,
                        Theme.withAlpha(Theme.TEXT_MUTED, alpha));
            }
        } else {
            String selected = getter.get() != null ? getter.get() : "";
            String displaySelected = truncateText(font, selected, maxTextWidth);
            RenderHelper.text(g, displaySelected, boxX + 4, textY, 5,
                    Theme.withAlpha(Theme.TEXT_MUTED, (int)(200 * alphaMultiplier)));
        }

        RenderHelper.disableScissor(g);
    }

    private String truncateText(net.minecraft.client.gui.Font font, String text, float maxWidth) {
        if (FontHelper.width(font, text) <= maxWidth) {
            return text;
        }
        String truncated = text;
        while (FontHelper.width(font, truncated + "..") > maxWidth && truncated.length() > 1) {
            truncated = truncated.substring(0, truncated.length() - 1);
        }
        return truncated + "..";
    }

    private void renderScrollingDescription(GuiGraphicsExtractor g, net.minecraft.client.gui.Font font,
                                            String description, float deltaTime) {
        float descY = y + height / 2 + 0.5f;
        float boxX = x + width - BOX_WIDTH - 2;
        float availableWidth = boxX - x - DESC_PADDING;
        float descWidth = FontHelper.width(font, description);

        if (descWidth <= availableWidth) {
            descScrollOffset = 0;
            RenderHelper.text(g, description, x + 0.5f, descY, 5,
                    Theme.withAlpha(Theme.TEXT_MUTED, (int)(128 * alphaMultiplier)));
        } else {
            updateDescScrollAnimation(deltaTime, descWidth, availableWidth);

            float maxScroll = descWidth - availableWidth + 5;
            float currentScroll = descScrollOffset * maxScroll;

            RenderHelper.enableScissor(g, x, descY - 2, availableWidth, 10);
            RenderHelper.text(g, description, x + 0.5f - currentScroll, descY, 5,
                    Theme.withAlpha(Theme.TEXT_MUTED, (int)(128 * alphaMultiplier)));
            RenderHelper.disableScissor(g);
        }
    }

    private void updateDescScrollAnimation(float deltaTime, float textWidth, float availableWidth) {
        long currentTime = System.currentTimeMillis();

        if (descScrollPauseTime > 0) {
            if (currentTime - descScrollPauseTime < SCROLL_PAUSE_DURATION) {
                return;
            }
            descScrollPauseTime = 0;
        }

        float scrollDistance = textWidth - availableWidth + 5;
        if (scrollDistance <= 0) {
            descScrollOffset = 0;
            return;
        }

        float scrollSpeed = SCROLL_PIXELS_PER_SECOND / scrollDistance;

        if (descScrollingRight) {
            descScrollOffset += deltaTime * scrollSpeed;
            if (descScrollOffset >= 1f) {
                descScrollOffset = 1f;
                descScrollingRight = false;
                descScrollPauseTime = currentTime;
            }
        } else {
            descScrollOffset -= deltaTime * scrollSpeed;
            if (descScrollOffset <= 0f) {
                descScrollOffset = 0f;
                descScrollingRight = true;
                descScrollPauseTime = currentTime;
            }
        }
    }

    private void renderExpandedOptions(GuiGraphicsExtractor g, net.minecraft.client.gui.Font font,
                                       int mouseX, int mouseY, float boxX, float startY, float deltaTime) {
        float fullPanelHeight = options.size() * OPTION_HEIGHT;
        float visibleHeight = fullPanelHeight * expandAnimation;

        float panelAlpha = expandAnimation * alphaMultiplier;

        int panelBgAlpha = (int)(200 * panelAlpha);
        RenderHelper.rect(g, boxX, startY, BOX_WIDTH, visibleHeight,
                Theme.withAlpha(Theme.BG_PANEL, panelBgAlpha));

        int panelOutlineAlpha = (int)(100 * panelAlpha);
        RenderHelper.outline(g, boxX, startY, BOX_WIDTH, visibleHeight, 0.5f,
                Theme.withAlpha(Theme.BORDER, panelOutlineAlpha));

        if (visibleHeight < 1f) return;

        RenderHelper.enableScissor(g, boxX, startY, BOX_WIDTH, visibleHeight);

        float optionY = startY;
        String currentSelected = getter.get() != null ? getter.get() : "";

        for (int i = 0; i < options.size(); i++) {
            String option = options.get(i);

            boolean optionHovered = mouseX >= boxX && mouseX <= boxX + BOX_WIDTH &&
                    mouseY >= optionY && mouseY <= optionY + OPTION_HEIGHT &&
                    expandAnimation > 0.8f;

            float hoverAnim = optionHoverAnimations.getOrDefault(option, 0f);
            hoverAnim = lerp(hoverAnim, optionHovered ? 1f : 0f, deltaTime * ANIMATION_SPEED);
            optionHoverAnimations.put(option, hoverAnim);

            boolean isSelected = option.equals(currentSelected);
            float selectAnim = selectAnimations.getOrDefault(option, 0f);
            selectAnim = lerp(selectAnim, isSelected ? 1f : 0f, deltaTime * 10f);
            selectAnimations.put(option, selectAnim);

            if (hoverAnim > 0.01f) {
                int hoverBgAlpha = (int)(30 * hoverAnim * panelAlpha);
                RenderHelper.rect(g, boxX + 2, optionY + 1, BOX_WIDTH - 4, OPTION_HEIGHT - 2,
                        Theme.withAlpha(Theme.BG_ENTRY_HOVER, hoverBgAlpha));
            }

            float checkSize = 6f;
            float checkX = boxX + 5;
            float checkY = optionY + OPTION_HEIGHT / 2 - checkSize / 2;

            int checkBgAlpha = (int)((40 + hoverAnim * 20) * panelAlpha);
            RenderHelper.rect(g, checkX, checkY, checkSize, checkSize,
                    Theme.withAlpha(Theme.BG_INPUT, checkBgAlpha));

            int checkOutlineAlpha = (int)((80 + hoverAnim * 40) * panelAlpha);
            RenderHelper.outline(g, checkX, checkY, checkSize, checkSize, 0.5f,
                    Theme.withAlpha(Theme.BORDER, checkOutlineAlpha));

            if (selectAnim > 0.01f) {
                float innerSize = (checkSize - 2) * selectAnim;
                float innerX = checkX + (checkSize - innerSize) / 2;
                float innerY = checkY + (checkSize - innerSize) / 2;

                int innerAlpha = (int)(220 * selectAnim * panelAlpha);
                RenderHelper.rect(g, innerX, innerY, innerSize, innerSize,
                        Theme.withAlpha(Theme.ACCENT, innerAlpha));
            }

            float textX = checkX + checkSize + 4;
            float textY = optionY + OPTION_HEIGHT / 2 - 2.5f;

            float availableTextWidth = BOX_WIDTH - checkSize - 14;
            String displayOption = truncateText(font, option, availableTextWidth);

            int textGray = (int)(140 + selectAnim * 40 + hoverAnim * 20);
            int textAlpha = (int)(200 * panelAlpha);
            int textColor = Theme.withAlpha(0xFF000000 | (textGray << 16) | (textGray << 8) | (textGray + 5), textAlpha);
            RenderHelper.text(g, displayOption, textX, textY, 5, textColor);

            optionY += OPTION_HEIGHT;
        }

        RenderHelper.disableScissor(g);
    }

    private boolean isMainHover(double mouseX, double mouseY) {
        float boxX = x + width - BOX_WIDTH - 2;
        float boxY = y + height / 2 - 5;
        float boxHeight = 10f;
        return mouseX >= boxX && mouseX <= boxX + BOX_WIDTH && mouseY >= boxY && mouseY <= boxY + boxHeight;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (isMainHover(mouseX, mouseY)) {
                expanded = !expanded;
                return true;
            }

            if (expanded && expandAnimation > 0.8f) {
                float boxX = x + width - BOX_WIDTH - 2;
                float boxY = y + height / 2 - 5;
                float startY = boxY + 10f + 2;

                float optionY = startY;
                for (String option : options) {
                    if (mouseX >= boxX && mouseX <= boxX + BOX_WIDTH &&
                            mouseY >= optionY && mouseY <= optionY + OPTION_HEIGHT) {
                        setter.accept(option);
                        expanded = false;
                        return true;
                    }
                    optionY += OPTION_HEIGHT;
                }
            }
        }
        return false;
    }

    @Override
    public void tick() {
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public float getTotalHeight() {
        float baseHeight = height;
        float expandedHeight = options.size() * OPTION_HEIGHT * expandAnimation;
        return baseHeight + expandedHeight;
    }
}
