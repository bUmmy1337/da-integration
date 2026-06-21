package net.bummy1337.daintegrate.fabric.gui.clickgui.impl.settingsrender;

import net.bummy1337.daintegrate.fabric.gui.FontHelper;
import net.bummy1337.daintegrate.fabric.gui.Theme;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MultiSelectComponent extends AbstractSettingComponent {
    private final String settingName;
    private final String settingDescription;
    private final List<String> options;
    private final java.util.function.Supplier<Set<String>> getter;
    private final java.util.function.Consumer<Set<String>> setter;

    private boolean expanded = false;
    private float expandAnimation = 0f;
    private float hoverAnimation = 0f;
    private float scrollOffset = 0f;
    private float scrollOffsetAnimated = 0f;
    private boolean scrollingRight = true;
    private long scrollPauseTime = 0;

    private float descScrollOffset = 0f;
    private boolean descScrollingRight = true;
    private long descScrollPauseTime = 0;

    private float arrowRotation = 0f;

    private final Map<String, Float> optionHoverAnimations = new HashMap<>();
    private final Map<String, Float> checkAnimations = new HashMap<>();
    private final Map<String, Float> itemAlphaAnimations = new HashMap<>();
    private final Map<String, Float> itemXPositions = new HashMap<>();
    private final Map<String, Float> itemTargetPositions = new HashMap<>();
    private final Set<String> previousSelected = new HashSet<>();

    private float noneAlphaAnimation = 0f;

    private long lastUpdateTime = System.currentTimeMillis();

    private static final float ANIMATION_SPEED = 8f;
    private static final float COLLAPSE_SPEED = 15f;
    private static final long SCROLL_PAUSE_DURATION = 2000;
    private static final float BOX_WIDTH = 65f;
    private static final float OPTION_HEIGHT = 14f;
    private static final float SCROLL_PIXELS_PER_SECOND = 20f;
    private static final float DESC_PADDING = 8f;
    private static final float ITEM_ANIMATION_SPEED = 10f;
    private static final float POSITION_ANIMATION_SPEED = 8f;

    public MultiSelectComponent(String name, String description, List<String> options,
                                java.util.function.Supplier<Set<String>> getter,
                                java.util.function.Consumer<Set<String>> setter) {
        super(name, description);
        this.settingName = name;
        this.settingDescription = description;
        this.options = options;
        this.getter = getter;
        this.setter = setter;
        for (String option : options) {
            checkAnimations.put(option, getter.get().contains(option) ? 1f : 0f);
            optionHoverAnimations.put(option, 0f);
        }
        previousSelected.addAll(getter.get());

        float initX = 0;
        var font = Minecraft.getInstance().font;
        for (String item : options) {
            if (getter.get().contains(item)) {
                itemAlphaAnimations.put(item, 1f);
                itemXPositions.put(item, initX);
                itemTargetPositions.put(item, initX);

                String displayText = item + ", ";
                initX += FontHelper.width(font, displayText);
            }
        }

        noneAlphaAnimation = getter.get().isEmpty() ? 1f : 0f;
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

    private void updateItemAnimations(float deltaTime) {
        Set<String> currentSelected = new HashSet<>(getter.get());
        var font = Minecraft.getInstance().font;

        for (String item : currentSelected) {
            if (!itemAlphaAnimations.containsKey(item)) {
                itemAlphaAnimations.put(item, 0f);

                float lastPos = 0;
                for (String existingItem : options) {
                    if (itemXPositions.containsKey(existingItem)) {
                        float pos = itemXPositions.get(existingItem);
                        String text = existingItem + ", ";
                        float endPos = pos + FontHelper.width(font, text);
                        if (endPos > lastPos) {
                            lastPos = endPos;
                        }
                    }
                }
                itemXPositions.put(item, lastPos);
                itemTargetPositions.put(item, lastPos);
            }
        }

        for (String item : itemAlphaAnimations.keySet()) {
            boolean isSelected = currentSelected.contains(item);
            float currentAlpha = itemAlphaAnimations.get(item);
            float targetAlpha = isSelected ? 1f : 0f;
            float newAlpha = lerp(currentAlpha, targetAlpha, deltaTime * ITEM_ANIMATION_SPEED);
            itemAlphaAnimations.put(item, newAlpha);
        }

        List<String> allItems = options;
        List<String> visibleItems = new ArrayList<>();

        for (String item : allItems) {
            if (itemAlphaAnimations.containsKey(item) && itemAlphaAnimations.get(item) > 0.01f) {
                visibleItems.add(item);
            }
        }

        float currentTargetX = 0;
        for (int i = 0; i < visibleItems.size(); i++) {
            String item = visibleItems.get(i);
            float itemAlpha = itemAlphaAnimations.getOrDefault(item, 0f);

            itemTargetPositions.put(item, currentTargetX);

            String displayText = item;
            if (i < visibleItems.size() - 1) {
                displayText += ", ";
            }

            float textWidth = FontHelper.width(font, displayText);
            currentTargetX += textWidth * itemAlpha;
        }

        for (String item : visibleItems) {
            float targetX = itemTargetPositions.getOrDefault(item, 0f);
            float currentX = itemXPositions.getOrDefault(item, targetX);
            currentX = lerp(currentX, targetX, deltaTime * POSITION_ANIMATION_SPEED);
            itemXPositions.put(item, currentX);
        }

        List<String> toRemove = new ArrayList<>();
        for (String item : itemAlphaAnimations.keySet()) {
            boolean isSelected = currentSelected.contains(item);
            float alpha = itemAlphaAnimations.get(item);
            if (!isSelected && alpha < 0.01f) {
                toRemove.add(item);
            }
        }

        for (String item : toRemove) {
            itemAlphaAnimations.remove(item);
            itemXPositions.remove(item);
            itemTargetPositions.remove(item);
        }

        boolean hasVisibleItems = false;
        for (Float alpha : itemAlphaAnimations.values()) {
            if (alpha > 0.01f) {
                hasVisibleItems = true;
                break;
            }
        }

        float noneTarget = (!hasVisibleItems && currentSelected.isEmpty()) ? 1f : 0f;
        noneAlphaAnimation = lerp(noneAlphaAnimation, noneTarget, deltaTime * ITEM_ANIMATION_SPEED);

        previousSelected.clear();
        previousSelected.addAll(currentSelected);
    }

    @Override
    public void render(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        float deltaTime = getDeltaTime();
        var font = Minecraft.getInstance().font;

        updateItemAnimations(deltaTime);

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

        renderSelectedText(g, font, boxX, boxY, BOX_WIDTH, boxHeight, deltaTime);

        renderArrowIcon(g, boxX + BOX_WIDTH - 8, boxY + boxHeight / 2 - 4f);

        if (expandAnimation > 0.01f) {
            renderExpandedOptions(g, font, mouseX, mouseY, boxX, boxY + boxHeight + 2, deltaTime);
        }
    }

    private void renderArrowIcon(GuiGraphicsExtractor g, float iconX, float iconY) {
        int arrowAlpha = 120 + (int)(hoverAnimation * 60);

        float centerX = iconX + 4f;
        float centerY = iconY + 4f;

        float rad = (float) Math.toRadians(arrowRotation);
        float cos = (float) Math.cos(rad);
        float sin = (float) Math.sin(rad);

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

    private void renderSelectedText(GuiGraphicsExtractor g, net.minecraft.client.gui.Font font,
                                    float boxX, float boxY, float boxWidth, float boxHeight, float deltaTime) {
        float textY = boxY + boxHeight / 2 - 2.5f;
        float availableWidth = boxWidth - 4;
        float baseX = boxX + 4;

        RenderHelper.enableScissor(g, boxX + 1, boxY, availableWidth + 2, boxHeight);

        if (noneAlphaAnimation > 0.01f) {
            int noneAlpha = (int)(200 * noneAlphaAnimation * alphaMultiplier);
            RenderHelper.text(g, "None", baseX, textY, 5,
                    Theme.withAlpha(Theme.TEXT_MUTED, noneAlpha));
        }

        List<String> visibleItems = new ArrayList<>();
        for (String item : options) {
            if (itemAlphaAnimations.containsKey(item) && itemAlphaAnimations.get(item) > 0.01f) {
                visibleItems.add(item);
            }
        }

        if (visibleItems.isEmpty()) {
            RenderHelper.disableScissor(g);
            return;
        }

        float totalWidth = 0;
        for (int i = 0; i < visibleItems.size(); i++) {
            String item = visibleItems.get(i);
            float itemAlpha = itemAlphaAnimations.getOrDefault(item, 0f);

            String displayText = item;
            if (i < visibleItems.size() - 1) {
                displayText += ", ";
            }
            totalWidth += FontHelper.width(font, displayText) * itemAlpha;
        }

        if (totalWidth <= availableWidth) {
            scrollOffset = 0;
            scrollOffsetAnimated = lerp(scrollOffsetAnimated, 0, deltaTime * POSITION_ANIMATION_SPEED);
        } else {
            updateScrollAnimation(deltaTime, totalWidth, availableWidth);
            scrollOffsetAnimated = lerp(scrollOffsetAnimated, scrollOffset, deltaTime * POSITION_ANIMATION_SPEED);
        }

        float maxScroll = Math.max(0, totalWidth - availableWidth + 5);
        float currentScroll = scrollOffsetAnimated * maxScroll;

        for (int i = 0; i < visibleItems.size(); i++) {
            String item = visibleItems.get(i);
            float itemAlpha = itemAlphaAnimations.getOrDefault(item, 0f);
            float itemX = itemXPositions.getOrDefault(item, 0f);

            String displayText = item;
            if (i < visibleItems.size() - 1) {
                displayText += ", ";
            }

            float renderX = baseX + itemX - currentScroll;

            int alpha = (int)(200 * itemAlpha * alphaMultiplier);
            if (alpha > 0) {
                RenderHelper.text(g, displayText, renderX, textY, 5,
                        Theme.withAlpha(Theme.TEXT_MUTED, alpha));
            }
        }

        RenderHelper.disableScissor(g);
    }

    private void updateScrollAnimation(float deltaTime, float textWidth, float availableWidth) {
        long currentTime = System.currentTimeMillis();

        if (scrollPauseTime > 0) {
            if (currentTime - scrollPauseTime < SCROLL_PAUSE_DURATION) {
                return;
            }
            scrollPauseTime = 0;
        }

        float scrollDistance = textWidth - availableWidth + 5;
        if (scrollDistance <= 0) {
            scrollOffset = 0;
            return;
        }

        float scrollSpeed = SCROLL_PIXELS_PER_SECOND / scrollDistance;

        if (scrollingRight) {
            scrollOffset += deltaTime * scrollSpeed;
            if (scrollOffset >= 1f) {
                scrollOffset = 1f;
                scrollingRight = false;
                scrollPauseTime = currentTime;
            }
        } else {
            scrollOffset -= deltaTime * scrollSpeed;
            if (scrollOffset <= 0f) {
                scrollOffset = 0f;
                scrollingRight = true;
                scrollPauseTime = currentTime;
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
        Set<String> currentSelected = getter.get();

        for (int i = 0; i < options.size(); i++) {
            String option = options.get(i);

            boolean optionHovered = mouseX >= boxX && mouseX <= boxX + BOX_WIDTH &&
                    mouseY >= optionY && mouseY <= optionY + OPTION_HEIGHT &&
                    expandAnimation > 0.8f;

            float hoverAnim = optionHoverAnimations.getOrDefault(option, 0f);
            hoverAnim = lerp(hoverAnim, optionHovered ? 1f : 0f, deltaTime * ANIMATION_SPEED);
            optionHoverAnimations.put(option, hoverAnim);

            boolean isSelected = currentSelected.contains(option);
            float checkAnim = checkAnimations.getOrDefault(option, 0f);
            checkAnim = lerp(checkAnim, isSelected ? 1f : 0f, deltaTime * 10f);
            checkAnimations.put(option, checkAnim);

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

            if (checkAnim > 0.01f) {
                float innerSize = (checkSize - 2) * checkAnim;
                float innerX = checkX + (checkSize - innerSize) / 2;
                float innerY = checkY + (checkSize - innerSize) / 2;

                int innerAlpha = (int)(220 * checkAnim * panelAlpha);
                RenderHelper.rect(g, innerX, innerY, innerSize, innerSize,
                        Theme.withAlpha(Theme.ACCENT, innerAlpha));
            }

            float textX = checkX + checkSize + 4;
            float textY = optionY + OPTION_HEIGHT / 2 - 2.5f;

            float availableTextWidth = BOX_WIDTH - checkSize - 14;
            String displayOption = truncateText(font, option, availableTextWidth);

            int textGray = (int)(140 + checkAnim * 40 + hoverAnim * 20);
            int textAlpha = (int)(200 * panelAlpha);
            int textColor = Theme.withAlpha(0xFF000000 | (textGray << 16) | (textGray << 8) | (textGray + 5), textAlpha);
            RenderHelper.text(g, displayOption, textX, textY, 5, textColor);

            optionY += OPTION_HEIGHT;
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
                        Set<String> current = new HashSet<>(getter.get());
                        if (current.contains(option)) {
                            current.remove(option);
                        } else {
                            current.add(option);
                        }
                        setter.accept(current);
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
