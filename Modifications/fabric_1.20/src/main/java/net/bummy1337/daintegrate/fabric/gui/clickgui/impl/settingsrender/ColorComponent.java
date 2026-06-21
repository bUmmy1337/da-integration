package net.bummy1337.daintegrate.fabric.gui.clickgui.impl.settingsrender;

import net.bummy1337.daintegrate.fabric.gui.FontHelper;
import net.bummy1337.daintegrate.fabric.gui.Theme;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

import java.awt.*;

public class ColorComponent extends AbstractSettingComponent {
    private final String settingName;
    private final String settingDescription;
    private final java.util.function.Supplier<Integer> getter;
    private final java.util.function.Consumer<Integer> setter;

    private boolean expanded = false;
    private float expandAnimation = 0f;
    private float hoverAnimation = 0f;
    private float previewHoverAnimation = 0f;
    private float contentAlpha = 0f;

    private boolean draggingPalette = false;
    private boolean draggingHue = false;
    private boolean draggingAlpha = false;

    private float paletteHandleAnimation = 0f;
    private float hueHandleAnimation = 0f;
    private float alphaHandleAnimation = 0f;

    private boolean hexInputActive = false;
    private String hexInputText = "";
    private int hexCursorPosition = 0;
    private int hexSelectionStart = -1;
    private int hexSelectionEnd = -1;
    private float hexInputAnimation = 0f;
    private float hexSelectionAnimation = 0f;
    private float hexCursorBlinkAnimation = 0f;

    private float displayHue;
    private float displaySaturation;
    private float displayBrightness;
    private float displayAlpha;
    private boolean colorInitialized = false;

    private long lastUpdateTime = System.currentTimeMillis();

    private static final float ANIMATION_SPEED = 8f;
    private static final float FAST_ANIMATION_SPEED = 15f;
    private static final float COLOR_TRANSITION_SPEED = 6f;
    private static final float CONTENT_FADE_SPEED = 15f;
    private static final float PALETTE_SIZE = 70f;
    private static final float SLIDER_WIDTH = 8f;
    private static final float SPACING = 4f;
    private static final float PREVIEW_SIZE = 12f;

    public ColorComponent(String name, String description, int defaultColor,
                          java.util.function.Supplier<Integer> getter,
                          java.util.function.Consumer<Integer> setter) {
        super(name, description);
        this.settingName = name;
        this.settingDescription = description;
        this.getter = getter;
        this.setter = setter;
        int c = getter.get();
        float r = ((c >> 16) & 0xFF) / 255f;
        float g = ((c >> 8) & 0xFF) / 255f;
        float b = (c & 0xFF) / 255f;
        float a = ((c >> 24) & 0xFF) / 255f;
        float[] hsb = Color.RGBtoHSB((int)(r * 255), (int)(g * 255), (int)(b * 255), null);
        displayHue = hsb[0];
        displaySaturation = hsb[1];
        displayBrightness = hsb[2];
        displayAlpha = a;
        colorInitialized = true;
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

    private float lerpHue(float current, float target, float speed) {
        float diff = target - current;
        if (diff > 0.5f) diff -= 1f;
        else if (diff < -0.5f) diff += 1f;
        if (Math.abs(diff) < 0.001f) return target;
        float result = current + diff * Math.min(speed, 1f);
        if (result < 0f) result += 1f;
        if (result > 1f) result -= 1f;
        return result;
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    private void updateDisplayColors(float deltaTime) {
        if (!colorInitialized) return;
        int c = getter.get();
        float a = ((c >> 24) & 0xFF) / 255f;
        float r = ((c >> 16) & 0xFF) / 255f;
        float g = ((c >> 8) & 0xFF) / 255f;
        float b = (c & 0xFF) / 255f;
        float[] hsb = Color.RGBtoHSB((int)(r * 255), (int)(g * 255), (int)(b * 255), null);
        float targetHue = hsb[0];
        float targetSat = hsb[1];
        float targetBri = hsb[2];
        float targetAlpha = a;

        float speed = deltaTime * COLOR_TRANSITION_SPEED;
        if (draggingPalette || draggingHue || draggingAlpha) {
            displayHue = targetHue;
            displaySaturation = targetSat;
            displayBrightness = targetBri;
            displayAlpha = targetAlpha;
        } else {
            displayHue = lerpHue(displayHue, targetHue, speed);
            displaySaturation = lerp(displaySaturation, targetSat, speed);
            displayBrightness = lerp(displayBrightness, targetBri, speed);
            displayAlpha = lerp(displayAlpha, targetAlpha, speed);
        }
    }

    private int getDisplayColor() {
        int rgb = Color.HSBtoRGB(displayHue, displaySaturation, displayBrightness);
        int alphaInt = Math.round(displayAlpha * 255);
        return (alphaInt << 24) | (rgb & 0x00FFFFFF);
    }

    private int getDisplayColorNoAlpha() {
        return Color.HSBtoRGB(displayHue, displaySaturation, displayBrightness) | 0xFF000000;
    }

    private int applyContentAlpha(int color) {
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        int newAlpha = clamp((int)(a * alphaMultiplier * contentAlpha));
        return (newAlpha << 24) | (r << 16) | (g << 8) | b;
    }

    private int applyContentAlpha(Color color) {
        int newAlpha = clamp((int)(color.getAlpha() * alphaMultiplier * contentAlpha));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), newAlpha).getRGB();
    }

    private int applyContentAlpha(Color color, float extraAlpha) {
        int newAlpha = clamp((int)(color.getAlpha() * alphaMultiplier * contentAlpha * extraAlpha));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), newAlpha).getRGB();
    }

    private boolean isControlDown() {
        long window = Minecraft.getInstance().getWindow().handle();
        return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }

    private boolean isShiftDown() {
        long window = Minecraft.getInstance().getWindow().handle();
        return GLFW.glfwGetKey(window, GLFW.GLFW_KEY_LEFT_SHIFT) == GLFW.GLFW_PRESS ||
                GLFW.glfwGetKey(window, GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
    }

    private boolean hasHexSelection() {
        return hexSelectionStart != -1 && hexSelectionEnd != -1 && hexSelectionStart != hexSelectionEnd;
    }

    private int getHexSelectionStart() {
        return Math.min(hexSelectionStart, hexSelectionEnd);
    }

    private int getHexSelectionEnd() {
        return Math.max(hexSelectionStart, hexSelectionEnd);
    }

    private String getHexSelectedText() {
        if (!hasHexSelection()) return "";
        return hexInputText.substring(getHexSelectionStart(), getHexSelectionEnd());
    }

    private void clearHexSelection() {
        hexSelectionStart = -1;
        hexSelectionEnd = -1;
    }

    private void selectAllHexText() {
        hexSelectionStart = 0;
        hexSelectionEnd = hexInputText.length();
        hexCursorPosition = hexInputText.length();
    }

    private void deleteHexSelectedText() {
        if (hasHexSelection()) {
            int start = getHexSelectionStart();
            int end = getHexSelectionEnd();
            hexInputText = hexInputText.substring(0, start) + hexInputText.substring(end);
            hexCursorPosition = start;
            clearHexSelection();
        }
    }

    private void pasteHexFromClipboard() {
        String clipboardText = GLFW.glfwGetClipboardString(Minecraft.getInstance().getWindow().handle());
        if (clipboardText != null && !clipboardText.isEmpty()) {
            clipboardText = clipboardText.replace("#", "").replaceAll("[^0-9A-Fa-f]", "").toUpperCase();
            if (hasHexSelection()) deleteHexSelectedText();
            int remainingSpace = 8 - hexInputText.length();
            if (clipboardText.length() > remainingSpace)
                clipboardText = clipboardText.substring(0, remainingSpace);
            if (!clipboardText.isEmpty()) {
                hexInputText = hexInputText.substring(0, hexCursorPosition) + clipboardText + hexInputText.substring(hexCursorPosition);
                hexCursorPosition += clipboardText.length();
            }
        }
    }

    private void copyHexToClipboard() {
        if (hasHexSelection()) {
            GLFW.glfwSetClipboardString(Minecraft.getInstance().getWindow().handle(), "#" + getHexSelectedText());
        } else if (!hexInputText.isEmpty()) {
            GLFW.glfwSetClipboardString(Minecraft.getInstance().getWindow().handle(), "#" + hexInputText);
        }
    }

    private void moveHexCursor(int direction) {
        if (hasHexSelection() && !isShiftDown()) {
            hexCursorPosition = direction < 0 ? getHexSelectionStart() : getHexSelectionEnd();
            clearHexSelection();
        } else {
            if (direction < 0 && hexCursorPosition > 0) hexCursorPosition--;
            else if (direction > 0 && hexCursorPosition < hexInputText.length()) hexCursorPosition++;
            updateHexSelectionAfterCursorMove();
        }
    }

    private void updateHexSelectionAfterCursorMove() {
        if (isShiftDown()) {
            if (hexSelectionStart == -1) hexSelectionStart = hexSelectionEnd != -1 ? hexSelectionEnd : hexCursorPosition;
            hexSelectionEnd = hexCursorPosition;
        } else {
            clearHexSelection();
        }
    }

    @Override
    public void render(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        float deltaTime = getDeltaTime();
        var font = Minecraft.getInstance().font;

        updateDisplayColors(deltaTime);
        if (draggingPalette) updatePalette(mouseX, mouseY);
        if (draggingHue) updateHue(mouseY);
        if (draggingAlpha) updateAlpha(mouseY);

        boolean hovered = isHover(mouseX, mouseY);
        boolean previewHovered = isPreviewHover(mouseX, mouseY);

        hoverAnimation = lerp(hoverAnimation, hovered ? 1f : 0f, deltaTime * ANIMATION_SPEED);
        previewHoverAnimation = lerp(previewHoverAnimation, previewHovered ? 1f : 0f, deltaTime * ANIMATION_SPEED);
        expandAnimation = lerp(expandAnimation, expanded ? 1f : 0f, deltaTime * ANIMATION_SPEED);
        hexInputAnimation = lerp(hexInputAnimation, hexInputActive ? 1f : 0f, deltaTime * FAST_ANIMATION_SPEED);
        hexSelectionAnimation = lerp(hexSelectionAnimation, hasHexSelection() ? 1f : 0f, deltaTime * ANIMATION_SPEED);

        if (hexInputActive) {
            hexCursorBlinkAnimation += deltaTime * 2f;
            if (hexCursorBlinkAnimation > 1f) hexCursorBlinkAnimation -= 1f;
        } else {
            hexCursorBlinkAnimation = 0f;
        }

        float contentAlphaTarget = expanded ? 1f : 0f;
        float contentAlphaSpeed = expanded ? CONTENT_FADE_SPEED : CONTENT_FADE_SPEED * 1.5f;
        contentAlpha = lerp(contentAlpha, contentAlphaTarget, deltaTime * contentAlphaSpeed);

        paletteHandleAnimation = lerp(paletteHandleAnimation, draggingPalette ? 1f : 0f, deltaTime * FAST_ANIMATION_SPEED);
        hueHandleAnimation = lerp(hueHandleAnimation, draggingHue ? 1f : 0f, deltaTime * FAST_ANIMATION_SPEED);
        alphaHandleAnimation = lerp(alphaHandleAnimation, draggingAlpha ? 1f : 0f, deltaTime * FAST_ANIMATION_SPEED);

        RenderHelper.text(g, settingName, x + 11.5f, y + height / 2 - 6.5f, 6,
                Theme.withAlpha(Theme.TEXT_PRIMARY, (int)(200 * alphaMultiplier)));

        if (settingDescription != null && !settingDescription.isEmpty()) {
            RenderHelper.text(g, settingDescription, x + 8.5f, y + height / 2 + 0.5f, 5,
                    Theme.withAlpha(Theme.TEXT_MUTED, (int)(128 * alphaMultiplier)));
        }

        renderColorPreview(g, mouseX, mouseY);

        if (expandAnimation > 0.01f) {
            renderColorPicker(g, font, mouseX, mouseY, deltaTime);
        }
    }

    private void renderColorPreview(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        float previewX = x + width - 14;
        float previewY = y + height / 2 / 2;

        float scale = 1f + previewHoverAnimation * 0.1f;
        float scaledX = previewX - scale / 2 + 1;
        float scaledY = previewY - scale / 2;

        int colorValue = getDisplayColor();
        RenderHelper.rect(g, scaledX + 0.5f, scaledY + 0.5f, 9, 9,
                applyContentAlpha(new Color(colorValue, true)));
        int outlineAlpha = clamp((int)((255 + previewHoverAnimation * 60) * alphaMultiplier));
        RenderHelper.outline(g, scaledX, scaledY, 10, 10, 1f,
                Theme.withAlpha(Theme.BORDER, outlineAlpha));
    }

    private void renderColorPicker(GuiGraphicsExtractor g, net.minecraft.client.gui.Font font,
                                   int mouseX, int mouseY, float deltaTime) {
        float pickerX = x;
        float pickerY = y + height + SPACING;
        float pickerWidth = width;

        float totalExpandedHeight = PALETTE_SIZE + SPACING + 18 + SPACING;
        float visibleHeight = totalExpandedHeight * expandAnimation;

        int outlineAlpha = clamp((int)(60 * expandAnimation * contentAlpha * alphaMultiplier));
        RenderHelper.outline(g, pickerX, pickerY, pickerWidth, visibleHeight + 2, 0.5f,
                Theme.withAlpha(Theme.BORDER, outlineAlpha));

        if (expandAnimation < 0.3f || contentAlpha < 0.01f) return;

        RenderHelper.enableScissor(g, pickerX, pickerY, pickerWidth, visibleHeight);

        float contentX = pickerX + SPACING;
        float contentY = pickerY + SPACING;
        float contentWidth = pickerWidth - SPACING * 2;

        float slidersWidth = SLIDER_WIDTH * 2 + SPACING;
        float paletteWidth = contentWidth - slidersWidth - SPACING;

        renderHueSlider(g, contentX, contentY, SLIDER_WIDTH, PALETTE_SIZE, mouseX, mouseY);
        renderAlphaSlider(g, contentX + SLIDER_WIDTH + SPACING, contentY, SLIDER_WIDTH, PALETTE_SIZE, mouseX, mouseY);
        renderSaturationBrightnessPalette(g, contentX + slidersWidth + SPACING, contentY, paletteWidth, PALETTE_SIZE, mouseX, mouseY);

        contentY += PALETTE_SIZE + SPACING;
        renderHexInput(g, font, contentX, contentY, contentWidth, 16, mouseX, mouseY);

        RenderHelper.disableScissor(g);
    }

    private void renderSaturationBrightnessPalette(GuiGraphicsExtractor g, float paletteX, float paletteY,
                                                   float paletteWidth, float paletteHeight, int mouseX, int mouseY) {
        int pureColor = Color.HSBtoRGB(displayHue, 1f, 1f);
        Color pure = new Color(pureColor);

        int[] gradientColors = {
                applyContentAlpha(Color.WHITE),
                applyContentAlpha(pure),
                applyContentAlpha(pure),
                applyContentAlpha(Color.WHITE)
        };
        RenderHelper.gradientRect(g, paletteX, paletteY, paletteWidth, paletteHeight - 0.5f, gradientColors);

        int[] blackGradient = {
                new Color(0, 0, 0, 0).getRGB(),
                new Color(0, 0, 0, 0).getRGB(),
                applyContentAlpha(Color.BLACK),
                applyContentAlpha(Color.BLACK)
        };
        RenderHelper.gradientRect(g, paletteX, paletteY, paletteWidth, paletteHeight, blackGradient);

        float handleX = paletteX + displaySaturation * paletteWidth;
        float handleY = paletteY + (1f - displayBrightness) * paletteHeight;
        float handleSize = 6f + paletteHandleAnimation * 2f;

        int handleOutlineAlpha = clamp((int)(255 * expandAnimation * contentAlpha * alphaMultiplier));
        RenderHelper.rect(g, handleX - handleSize / 2, handleY - handleSize / 2, handleSize, handleSize,
                Theme.withAlpha(Theme.WHITE, handleOutlineAlpha));

        int currentColor = Color.HSBtoRGB(displayHue, displaySaturation, displayBrightness);
        RenderHelper.rect(g, handleX - handleSize / 2 + 1, handleY - handleSize / 2 + 1, handleSize - 2, handleSize - 2,
                applyContentAlpha(new Color(currentColor)));
    }

    private void renderHueSlider(GuiGraphicsExtractor g, float sliderX, float sliderY,
                                 float sliderWidth, float sliderHeight, int mouseX, int mouseY) {
        int[] hueColors = {
                Color.HSBtoRGB(0f, 1f, 1f),
                Color.HSBtoRGB(1f / 6f, 1f, 1f),
                Color.HSBtoRGB(2f / 6f, 1f, 1f),
                Color.HSBtoRGB(3f / 6f, 1f, 1f),
                Color.HSBtoRGB(4f / 6f, 1f, 1f),
                Color.HSBtoRGB(5f / 6f, 1f, 1f),
                Color.HSBtoRGB(1f, 1f, 1f)
        };

        float segmentHeight = sliderHeight / 6f;

        int[] colorsTop = {
                applyContentAlpha(new Color(hueColors[0])),
                applyContentAlpha(new Color(hueColors[0])),
                applyContentAlpha(new Color(hueColors[1])),
                applyContentAlpha(new Color(hueColors[1]))
        };
        RenderHelper.gradientRect(g, sliderX, sliderY, sliderWidth, segmentHeight, colorsTop);

        for (int i = 1; i < 5; i++) {
            float segY = sliderY + i * segmentHeight;
            int[] colors = {
                    applyContentAlpha(new Color(hueColors[i])),
                    applyContentAlpha(new Color(hueColors[i])),
                    applyContentAlpha(new Color(hueColors[i + 1])),
                    applyContentAlpha(new Color(hueColors[i + 1]))
            };
            RenderHelper.gradientRect(g, sliderX, segY - 0.5f, sliderWidth, segmentHeight + 0.5f, colors);
        }

        int[] colorsBottom = {
                applyContentAlpha(new Color(hueColors[5])),
                applyContentAlpha(new Color(hueColors[5])),
                applyContentAlpha(new Color(hueColors[6])),
                applyContentAlpha(new Color(hueColors[6]))
        };
        RenderHelper.gradientRect(g, sliderX, sliderY + 5 * segmentHeight - 0.5f, sliderWidth, segmentHeight, colorsBottom);

        int hueOutlineAlpha = clamp((int)(80 * expandAnimation * contentAlpha * alphaMultiplier));
        RenderHelper.outline(g, sliderX, sliderY, sliderWidth, sliderHeight, 0.5f,
                Theme.withAlpha(Theme.BORDER, hueOutlineAlpha));

        float handleY = sliderY + displayHue * sliderHeight;
        float handleHeight = 3f + hueHandleAnimation * 1f;
        float handleWidth = sliderWidth + 2f;

        int handleAlpha = clamp((int)(255 * expandAnimation * contentAlpha * alphaMultiplier));
        RenderHelper.rect(g, sliderX - 1, handleY - handleHeight / 2, handleWidth, handleHeight,
                Theme.withAlpha(Theme.WHITE, handleAlpha));
        int handleShadowAlpha = clamp((int)(100 * expandAnimation * contentAlpha * alphaMultiplier));
        RenderHelper.outline(g, sliderX - 1, handleY - handleHeight / 2, handleWidth, handleHeight, 0.5f,
                Theme.withAlpha(Theme.BLACK, handleShadowAlpha));
    }

    private void renderAlphaSlider(GuiGraphicsExtractor g, float sliderX, float sliderY,
                                   float sliderWidth, float sliderHeight, int mouseX, int mouseY) {
        int checkAlpha = clamp((int)(150 * expandAnimation * contentAlpha * alphaMultiplier));
        RenderHelper.rect(g, sliderX, sliderY, sliderWidth, sliderHeight,
                Theme.withAlpha(Theme.TEXT_SECONDARY, checkAlpha));

        int baseColor = getDisplayColorNoAlpha() & 0x00FFFFFF;
        int transparentColor = baseColor;
        int opaqueColor = baseColor | 0xFF000000;

        int[] alphaGradient = {
                applyContentAlpha(new Color(transparentColor, true), 0f),
                applyContentAlpha(new Color(transparentColor, true), 0f),
                applyContentAlpha(new Color(opaqueColor, true)),
                applyContentAlpha(new Color(opaqueColor, true))
        };
        RenderHelper.gradientRect(g, sliderX, sliderY, sliderWidth, sliderHeight, alphaGradient);

        int alphaOutlineAlpha = clamp((int)(80 * expandAnimation * contentAlpha * alphaMultiplier));
        RenderHelper.outline(g, sliderX, sliderY, sliderWidth, sliderHeight, 0.5f,
                Theme.withAlpha(Theme.BORDER, alphaOutlineAlpha));

        float handleY = sliderY + displayAlpha * sliderHeight;
        float handleHeight = 3f + alphaHandleAnimation * 1f;
        float handleWidth = sliderWidth + 2f;

        int handleAlpha = clamp((int)(255 * expandAnimation * contentAlpha * alphaMultiplier));
        RenderHelper.rect(g, sliderX - 1, handleY - handleHeight / 2, handleWidth, handleHeight,
                Theme.withAlpha(Theme.WHITE, handleAlpha));
        int handleShadowAlpha = clamp((int)(100 * expandAnimation * contentAlpha * alphaMultiplier));
        RenderHelper.outline(g, sliderX - 1, handleY - handleHeight / 2, handleWidth, handleHeight, 0.5f,
                Theme.withAlpha(Theme.BLACK, handleShadowAlpha));
    }

    private void renderHexInput(GuiGraphicsExtractor g, net.minecraft.client.gui.Font font,
                                float inputX, float inputY, float inputWidth, float inputHeight,
                                int mouseX, int mouseY) {
        boolean inputHovered = mouseX >= inputX && mouseX <= inputX + inputWidth &&
                mouseY >= inputY && mouseY <= inputY + inputHeight;

        int bgAlpha = clamp((int)((40 + hexInputAnimation * 20 + (inputHovered ? 10 : 0)) * expandAnimation * contentAlpha * alphaMultiplier));
        RenderHelper.rect(g, inputX, inputY, inputWidth, inputHeight,
                Theme.withAlpha(Theme.BG_INPUT, bgAlpha));

        int hexOutlineAlpha = clamp((int)((60 + hexInputAnimation * 80 + (inputHovered ? 20 : 0)) * expandAnimation * contentAlpha * alphaMultiplier));
        int outlineColor = hexInputActive ? Theme.BORDER_FOCUS : Theme.BORDER;
        RenderHelper.outline(g, inputX, inputY, inputWidth, inputHeight, 0.5f,
                Theme.withAlpha(outlineColor, hexOutlineAlpha));

        String label = "HEX: ";
        float iconOffset = 10f;
        float labelWidth = FontHelper.width(font, label);
        int labelAlpha = clamp((int)(150 * expandAnimation * contentAlpha * alphaMultiplier));
        RenderHelper.text(g, label, inputX + 4 + iconOffset, inputY + inputHeight / 2 - 2.5f, 5,
                Theme.withAlpha(Theme.TEXT_MUTED, labelAlpha));

        String displayText = hexInputActive ? hexInputText : getDisplayHexString();
        float textStartX = inputX + 4 + iconOffset + labelWidth;
        float textY = inputY + inputHeight / 2 - 2.5f;

        int textAlpha = clamp((int)((180 + hexInputAnimation * 40) * expandAnimation * contentAlpha * alphaMultiplier));
        RenderHelper.text(g, "#" + displayText, textStartX, textY, 5,
                Theme.withAlpha(Theme.TEXT_PRIMARY, textAlpha));

        if (hexInputActive && !hasHexSelection()) {
            float cursorAlpha = (float)(Math.sin(hexCursorBlinkAnimation * Math.PI * 2) * 0.5 + 0.5);
            if (cursorAlpha > 0.3f) {
                String beforeCursor = "#" + hexInputText.substring(0, hexCursorPosition);
                float cursorX = textStartX + FontHelper.width(font, beforeCursor);
                int cursorAlphaInt = clamp((int)(255 * cursorAlpha * hexInputAnimation * expandAnimation * contentAlpha * alphaMultiplier));
                RenderHelper.rect(g, cursorX, inputY + 3, 0.5f, inputHeight - 6,
                        Theme.withAlpha(Theme.TEXT_PRIMARY, cursorAlphaInt));
            }
        }

        float miniPreviewX = inputX + inputWidth - 15;
        float miniPreviewY = inputY + 3;
        float miniPreviewSize = inputHeight - 6;

        int miniCheckAlpha = clamp((int)(120 * expandAnimation * contentAlpha * alphaMultiplier));
        RenderHelper.rect(g, miniPreviewX, miniPreviewY, miniPreviewSize, miniPreviewSize,
                Theme.withAlpha(Theme.TEXT_SECONDARY, miniCheckAlpha));
        RenderHelper.rect(g, miniPreviewX, miniPreviewY, miniPreviewSize, miniPreviewSize,
                applyContentAlpha(new Color(getDisplayColor(), true)));
        int miniOutlineAlpha = clamp((int)(80 * expandAnimation * contentAlpha * alphaMultiplier));
        RenderHelper.outline(g, miniPreviewX, miniPreviewY, miniPreviewSize, miniPreviewSize, 0.5f,
                Theme.withAlpha(Theme.BORDER, miniOutlineAlpha));
    }

    private String getDisplayHexString() {
        int color = getDisplayColor();
        int a = (color >> 24) & 0xFF;
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return String.format("%02X%02X%02X%02X", r, g, b, a);
    }

    private boolean isPreviewHover(double mouseX, double mouseY) {
        float previewX = x + width - PREVIEW_SIZE - 4;
        float previewY = y + height / 2 - PREVIEW_SIZE / 2;
        return mouseX >= previewX && mouseX <= previewX + PREVIEW_SIZE &&
                mouseY >= previewY && mouseY <= previewY + PREVIEW_SIZE;
    }

    private boolean isPaletteHover(double mouseX, double mouseY) {
        float pickerX = x;
        float pickerY = y + height + SPACING;
        float contentX = pickerX + SPACING;
        float contentY = pickerY + SPACING;
        float contentWidth = width - SPACING * 2;
        float slidersWidth = SLIDER_WIDTH * 2 + SPACING;
        float paletteWidth = contentWidth - slidersWidth - SPACING;
        float paletteX = contentX + slidersWidth + SPACING;
        return mouseX >= paletteX && mouseX <= paletteX + paletteWidth &&
                mouseY >= contentY && mouseY <= contentY + PALETTE_SIZE;
    }

    private boolean isHueSliderHover(double mouseX, double mouseY) {
        float pickerX = x;
        float pickerY = y + height + SPACING;
        float contentX = pickerX + SPACING;
        float contentY = pickerY + SPACING;
        return mouseX >= contentX && mouseX <= contentX + SLIDER_WIDTH &&
                mouseY >= contentY && mouseY <= contentY + PALETTE_SIZE;
    }

    private boolean isAlphaSliderHover(double mouseX, double mouseY) {
        float pickerX = x;
        float pickerY = y + height + SPACING;
        float contentX = pickerX + SPACING;
        float contentY = pickerY + SPACING;
        float alphaSliderX = contentX + SLIDER_WIDTH + SPACING;
        return mouseX >= alphaSliderX && mouseX <= alphaSliderX + SLIDER_WIDTH &&
                mouseY >= contentY && mouseY <= contentY + PALETTE_SIZE;
    }

    private boolean isHexInputHover(double mouseX, double mouseY) {
        float pickerX = x;
        float pickerY = y + height + SPACING;
        float contentX = pickerX + SPACING;
        float contentY = pickerY + SPACING + PALETTE_SIZE + SPACING;
        float contentWidth = width - SPACING * 2;
        return mouseX >= contentX && mouseX <= contentX + contentWidth &&
                mouseY >= contentY && mouseY <= contentY + 16;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            if (isPreviewHover(mouseX, mouseY)) {
                expanded = !expanded;
                if (!expanded) {
                    hexInputActive = false;
                    draggingPalette = false;
                    draggingHue = false;
                    draggingAlpha = false;
                    clearHexSelection();
                }
                return true;
            }

            if (expanded && expandAnimation > 0.8f && contentAlpha > 0.5f) {
                if (isPaletteHover(mouseX, mouseY)) {
                    draggingPalette = true;
                    updatePalette(mouseX, mouseY);
                    hexInputActive = false;
                    clearHexSelection();
                    return true;
                }

                if (isHueSliderHover(mouseX, mouseY)) {
                    draggingHue = true;
                    updateHue(mouseY);
                    hexInputActive = false;
                    clearHexSelection();
                    return true;
                }

                if (isAlphaSliderHover(mouseX, mouseY)) {
                    draggingAlpha = true;
                    updateAlpha(mouseY);
                    hexInputActive = false;
                    clearHexSelection();
                    return true;
                }

                if (isHexInputHover(mouseX, mouseY)) {
                    hexInputActive = true;
                    hexInputText = getDisplayHexString();
                    hexCursorPosition = hexInputText.length();
                    hexSelectionStart = 0;
                    hexSelectionEnd = hexInputText.length();
                    return true;
                } else if (hexInputActive) {
                    applyHexInput();
                    hexInputActive = false;
                    clearHexSelection();
                }
            }
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) {
            boolean wasDragging = draggingPalette || draggingHue || draggingAlpha;
            draggingPalette = false;
            draggingHue = false;
            draggingAlpha = false;
            if (wasDragging) {
                updateHexFromColor();
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
        if (button == 0) {
            if (draggingPalette) { updatePalette(mouseX, mouseY); return true; }
            if (draggingHue) { updateHue(mouseY); return true; }
            if (draggingAlpha) { updateAlpha(mouseY); return true; }
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (!hexInputActive) return false;

        if (isControlDown()) {
            switch (keyCode) {
                case GLFW.GLFW_KEY_A -> { selectAllHexText(); return true; }
                case GLFW.GLFW_KEY_V -> { pasteHexFromClipboard(); return true; }
                case GLFW.GLFW_KEY_C -> { copyHexToClipboard(); return true; }
                case GLFW.GLFW_KEY_X -> {
                    if (hasHexSelection()) { copyHexToClipboard(); deleteHexSelectedText(); }
                    return true;
                }
            }
        }

        switch (keyCode) {
            case GLFW.GLFW_KEY_ENTER, GLFW.GLFW_KEY_KP_ENTER -> {
                applyHexInput(); hexInputActive = false; clearHexSelection(); return true;
            }
            case GLFW.GLFW_KEY_ESCAPE -> {
                hexInputActive = false; clearHexSelection(); return true;
            }
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (hasHexSelection()) deleteHexSelectedText();
                else if (hexCursorPosition > 0) {
                    hexInputText = hexInputText.substring(0, hexCursorPosition - 1) + hexInputText.substring(hexCursorPosition);
                    hexCursorPosition--;
                }
                return true;
            }
            case GLFW.GLFW_KEY_DELETE -> {
                if (hasHexSelection()) deleteHexSelectedText();
                else if (hexCursorPosition < hexInputText.length())
                    hexInputText = hexInputText.substring(0, hexCursorPosition) + hexInputText.substring(hexCursorPosition + 1);
                return true;
            }
            case GLFW.GLFW_KEY_LEFT -> { moveHexCursor(-1); return true; }
            case GLFW.GLFW_KEY_RIGHT -> { moveHexCursor(1); return true; }
            case GLFW.GLFW_KEY_HOME -> { hexCursorPosition = 0; updateHexSelectionAfterCursorMove(); return true; }
            case GLFW.GLFW_KEY_END -> { hexCursorPosition = hexInputText.length(); updateHexSelectionAfterCursorMove(); return true; }
        }
        return false;
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (!hexInputActive) return false;
        if (isHexChar(chr)) {
            if (hasHexSelection()) deleteHexSelectedText();
            if (hexInputText.length() < 8) {
                hexInputText = hexInputText.substring(0, hexCursorPosition) + Character.toUpperCase(chr) + hexInputText.substring(hexCursorPosition);
                hexCursorPosition++;
            }
            return true;
        }
        return false;
    }

    private boolean isHexChar(char c) {
        return (c >= '0' && c <= '9') || (c >= 'A' && c <= 'F') || (c >= 'a' && c <= 'f');
    }

    private void updatePalette(double mouseX, double mouseY) {
        float pickerX = x;
        float pickerY = y + height + SPACING;
        float contentX = pickerX + SPACING;
        float contentY = pickerY + SPACING;
        float contentWidth = width - SPACING * 2;
        float slidersWidth = SLIDER_WIDTH * 2 + SPACING;
        float paletteWidth = contentWidth - slidersWidth - SPACING;
        float paletteX = contentX + slidersWidth + SPACING;

        float saturation = (float)((mouseX - paletteX) / paletteWidth);
        float brightness = 1f - (float)((mouseY - contentY) / PALETTE_SIZE);

        int c = getter.get();
        float[] hsb = Color.RGBtoHSB((c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF, null);
        int newColor = Color.HSBtoRGB(hsb[0], Math.max(0, Math.min(1, saturation)), Math.max(0, Math.min(1, brightness)));
        int alpha = (c >> 24) & 0xFF;
        setter.accept((alpha << 24) | (newColor & 0x00FFFFFF));
    }

    private void updateHue(double mouseY) {
        float pickerY = y + height + SPACING;
        float contentY = pickerY + SPACING;
        float hue = Math.max(0, Math.min(1, (float)((mouseY - contentY) / PALETTE_SIZE)));

        int c = getter.get();
        float[] hsb = Color.RGBtoHSB((c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF, null);
        int newColor = Color.HSBtoRGB(hue, hsb[1], hsb[2]);
        int alpha = (c >> 24) & 0xFF;
        setter.accept((alpha << 24) | (newColor & 0x00FFFFFF));
    }

    private void updateAlpha(double mouseY) {
        float pickerY = y + height + SPACING;
        float contentY = pickerY + SPACING;
        float alpha = Math.max(0, Math.min(1, (float)((mouseY - contentY) / PALETTE_SIZE)));

        int c = getter.get();
        int rgb = c & 0x00FFFFFF;
        int alphaInt = Math.round(alpha * 255);
        setter.accept((alphaInt << 24) | rgb);
    }

    private void updateHexFromColor() {
        hexInputText = getDisplayHexString();
        hexCursorPosition = hexInputText.length();
    }

    private void applyHexInput() {
        String hex = hexInputText.toUpperCase();
        try {
            int r, g, b, a = 255;
            if (hex.length() == 6) {
                r = Integer.parseInt(hex.substring(0, 2), 16);
                g = Integer.parseInt(hex.substring(2, 4), 16);
                b = Integer.parseInt(hex.substring(4, 6), 16);
            } else if (hex.length() == 8) {
                r = Integer.parseInt(hex.substring(0, 2), 16);
                g = Integer.parseInt(hex.substring(2, 4), 16);
                b = Integer.parseInt(hex.substring(4, 6), 16);
                a = Integer.parseInt(hex.substring(6, 8), 16);
            } else if (hex.length() == 3) {
                r = Integer.parseInt(hex.substring(0, 1) + hex.substring(0, 1), 16);
                g = Integer.parseInt(hex.substring(1, 2) + hex.substring(1, 2), 16);
                b = Integer.parseInt(hex.substring(2, 3) + hex.substring(2, 3), 16);
            } else {
                updateHexFromColor();
                return;
            }
            int rgb = (r << 16) | (g << 8) | b;
            int color = (a << 24) | rgb;
            setter.accept(color);
        } catch (NumberFormatException e) {
            updateHexFromColor();
        }
    }

    @Override
    public void tick() {
    }

    @Override
    public boolean isHover(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public float getTotalHeight() {
        float totalExpandedHeight = PALETTE_SIZE + SPACING + 22 + SPACING * 2;
        float expandedHeight = totalExpandedHeight * expandAnimation;
        return height + expandedHeight;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public boolean isHexInputActive() {
        return hexInputActive;
    }

    public boolean isDragging() {
        return draggingPalette || draggingHue || draggingAlpha;
    }
}
