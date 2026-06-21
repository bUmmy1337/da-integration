package net.bummy1337.daintegrate.fabric.gui.clickgui.impl.settingsrender;

import net.bummy1337.daintegrate.fabric.gui.Theme;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.RenderHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class ButtonComponent extends AbstractSettingComponent {
    private final String settingName;
    private final String settingDescription;
    private final String buttonName;
    private final Runnable action;

    private float pressAnimation = 0f;
    private float hoverAnimation = 0f;
    private float scaleAnimation = 1f;
    private float rippleAnimation = 0f;
    private float rippleX = 0f;
    private float rippleY = 0f;
    private boolean wasPressed = false;
    private boolean rippleActive = false;

    private long lastUpdateTime = System.currentTimeMillis();

    private static final float ANIMATION_SPEED = 8f;
    private static final float FAST_ANIMATION_SPEED = 12f;
    private static final float BUTTON_WIDTH = 65f;
    private static final float BUTTON_HEIGHT = 12f;

    public ButtonComponent(String name, String description, String buttonName, Runnable action) {
        super(name, description);
        this.settingName = name;
        this.settingDescription = description;
        this.buttonName = buttonName;
        this.action = action;
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

    private int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }

    @Override
    public void render(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        float deltaTime = getDeltaTime();

        boolean hovered = isButtonHover(mouseX, mouseY);

        hoverAnimation = lerp(hoverAnimation, hovered ? 1f : 0f, deltaTime * ANIMATION_SPEED);

        float scaleTarget = wasPressed ? 0.95f : (hovered ? 1.02f : 1f);
        scaleAnimation = lerp(scaleAnimation, scaleTarget, deltaTime * FAST_ANIMATION_SPEED);

        pressAnimation = lerp(pressAnimation, wasPressed ? 1f : 0f, deltaTime * FAST_ANIMATION_SPEED);

        if (rippleActive) {
            rippleAnimation += deltaTime * 3f;
            if (rippleAnimation >= 1f) {
                rippleAnimation = 0f;
                rippleActive = false;
            }
        }

        if (pressAnimation < 0.05f && wasPressed) {
            wasPressed = false;
        }

        RenderHelper.text(g, settingName, x + 9.5f, y + height / 2 - 7.5f, 6,
                Theme.withAlpha(Theme.TEXT_PRIMARY, (int)(200 * alphaMultiplier)));

        if (settingDescription != null && !settingDescription.isEmpty()) {
            RenderHelper.text(g, settingDescription, x + 0.5f, y + height / 2 + 0.5f, 5,
                    Theme.withAlpha(Theme.TEXT_MUTED, (int)(128 * alphaMultiplier)));
        }

        renderButton(g, mouseX, mouseY);
    }

    private void renderButton(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        float buttonX = x + width - BUTTON_WIDTH - 2;
        float buttonY = y + height / 2 - BUTTON_HEIGHT / 2;

        float scaledWidth = BUTTON_WIDTH * scaleAnimation;
        float scaledHeight = BUTTON_HEIGHT * scaleAnimation;
        float scaledX = buttonX - (scaledWidth - BUTTON_WIDTH) / 2;
        float scaledY = buttonY - (scaledHeight - BUTTON_HEIGHT) / 2;

        float pressOffset = pressAnimation * 1f;
        scaledY += pressOffset;

        int bgAlpha = clamp((int)((30 + hoverAnimation * 20 + pressAnimation * 15) * alphaMultiplier));
        int bgGray = clamp((int)(35 + hoverAnimation * 15 + pressAnimation * 20));
        int bgColor = Theme.withAlpha(0xFF000000 | (bgGray << 16) | (bgGray << 8) | bgGray, bgAlpha);
        RenderHelper.rect(g, scaledX, scaledY, scaledWidth, scaledHeight, bgColor);

        if (rippleActive && rippleAnimation > 0) {
            float currentRippleSize = 20 * rippleAnimation;
            float rippleAlpha = (1f - rippleAnimation) * 0.4f;

            int rippleAlphaInt = clamp((int)(255 * rippleAlpha * alphaMultiplier));

            float localRippleX = rippleX - scaledX;
            float localRippleY = rippleY - scaledY;

            RenderHelper.rect(g,
                    scaledX + localRippleX - currentRippleSize / 2,
                    scaledY + localRippleY - currentRippleSize / 2,
                    currentRippleSize, currentRippleSize,
                    Theme.withAlpha(Theme.TEXT_PRIMARY, rippleAlphaInt));
        }

        int outlineAlpha = clamp((int)((60 + hoverAnimation * 60 + pressAnimation * 40) * alphaMultiplier));
        int outlineGray = clamp((int)(80 + hoverAnimation * 40 + pressAnimation * 30));
        int outlineColor = Theme.withAlpha(0xFF000000 | (outlineGray << 16) | (outlineGray << 8) | outlineGray, outlineAlpha);
        RenderHelper.outline(g, scaledX, scaledY, scaledWidth, scaledHeight, 0.5f, outlineColor);

        renderButtonContent(g, scaledX, scaledY, scaledWidth, scaledHeight);
    }

    private void renderButtonContent(GuiGraphicsExtractor g, float buttonX, float buttonY,
                                     float buttonWidth, float buttonHeight) {
        var font = net.minecraft.client.Minecraft.getInstance().font;
        String text = buttonName != null ? buttonName : "Run";

        float textWidth = net.bummy1337.daintegrate.fabric.gui.FontHelper.width(font, text);
        float startX = buttonX + (buttonWidth - textWidth) / 2;

        float textY = buttonY + buttonHeight / 2 - 3f;

        int textAlpha = clamp((int)((180 + hoverAnimation * 50 + pressAnimation * 25) * alphaMultiplier));
        int textGray = clamp((int)(180 + hoverAnimation * 40 + pressAnimation * 30));
        int textColor = Theme.withAlpha(0xFF000000 | (textGray << 16) | (textGray << 8) | textGray, textAlpha);
        RenderHelper.text(g, text, startX, textY, 5, textColor);
    }

    private boolean isButtonHover(double mouseX, double mouseY) {
        float buttonX = x + width - BUTTON_WIDTH - 2;
        float buttonY = y + height / 2 - BUTTON_HEIGHT / 2;
        return mouseX >= buttonX && mouseX <= buttonX + BUTTON_WIDTH &&
                mouseY >= buttonY && mouseY <= buttonY + BUTTON_HEIGHT;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isButtonHover(mouseX, mouseY) && button == 0) {
            if (action != null) {
                action.run();
            }
            wasPressed = true;
            pressAnimation = 1f;

            rippleActive = true;
            rippleAnimation = 0f;
            rippleX = (float) mouseX;
            rippleY = (float) mouseY;

            return true;
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
}
