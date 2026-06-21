package net.bummy1337.daintegrate.fabric.gui.clickgui.impl.settingsrender;

import net.bummy1337.daintegrate.fabric.gui.Theme;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.RenderHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class CheckboxComponent extends AbstractSettingComponent {
    private final String settingName;
    private final String settingDescription;
    private final java.util.function.Supplier<Boolean> getter;
    private final java.util.function.Consumer<Boolean> setter;

    private float checkAnimation = 0f;
    private float hoverAnimation = 0f;
    private float stretchAnimation = 0f;
    private float velocity = 0f;

    public CheckboxComponent(String name, String description,
                             java.util.function.Supplier<Boolean> getter,
                             java.util.function.Consumer<Boolean> setter) {
        super(name, description);
        this.settingName = name;
        this.settingDescription = description;
        this.getter = getter;
        this.setter = setter;
        this.checkAnimation = getter.get() ? 1f : 0f;
    }

    @Override
    public void render(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        boolean hovered = isHover(mouseX, mouseY);

        float hoverTarget = hovered ? 1f : 0f;
        hoverAnimation += (hoverTarget - hoverAnimation) * 0.2f;
        hoverAnimation = clamp(hoverAnimation, 0f, 1f);

        float target = getter.get() ? 1f : 0f;
        float oldCheck = checkAnimation;

        float speed = 0.35f;
        checkAnimation += (target - checkAnimation) * speed;

        if (Math.abs(target - checkAnimation) < 0.001f) {
            checkAnimation = target;
        }

        velocity = checkAnimation - oldCheck;

        float absVelocity = Math.abs(velocity);
        float targetStretch = absVelocity * 30f;
        targetStretch = clamp(targetStretch, 0f, 1f);

        float stretchSpeed = targetStretch > stretchAnimation ? 0.5f : 0.2f;
        stretchAnimation += (targetStretch - stretchAnimation) * stretchSpeed;
        stretchAnimation = clamp(stretchAnimation, 0f, 1f);

        RenderHelper.text(g, settingName, x + 9.5f, y + height / 2 - 7.5f, 6,
                Theme.withAlpha(Theme.TEXT_PRIMARY, (int)(200 * alphaMultiplier)));

        if (settingDescription != null && !settingDescription.isEmpty()) {
            RenderHelper.text(g, settingDescription, x + 0.5f, y + height / 2 + 0.5f, 5,
                    Theme.withAlpha(Theme.TEXT_MUTED, (int)(128 * alphaMultiplier)));
        }

        float checkboxSize = 10;
        float checkboxWidth = checkboxSize + 6;
        float checkboxX = x + width - checkboxWidth - 2;
        float checkboxY = y + height / 2 - checkboxSize / 2;

        RenderHelper.rect(g, checkboxX, checkboxY, checkboxWidth, checkboxSize,
                Theme.withAlpha(Theme.BG_INPUT, (int)(25 * alphaMultiplier)));

        int outlineAlpha = 60 + (int)(hoverAnimation * 40);
        RenderHelper.outline(g, checkboxX, checkboxY, checkboxWidth, checkboxSize, 0.5f,
                Theme.withAlpha(Theme.BORDER, (int)(outlineAlpha * alphaMultiplier)));

        float knobBaseSize = checkboxSize - 3;
        float maxStretchExtra = 4f;
        float stretchExtra = stretchAnimation * maxStretchExtra;

        float knobWidth = knobBaseSize + stretchExtra;
        float knobHeight = knobBaseSize - (stretchAnimation * 1f);

        float padding = 1.5f;
        float travelDistance = checkboxWidth - knobBaseSize - (padding * 2);

        float knobBaseX = checkboxX + padding;

        float stretchOffset;
        if (velocity > 0) {
            stretchOffset = -stretchExtra * 0.3f;
        } else if (velocity < 0) {
            stretchOffset = stretchExtra * 0.3f;
        } else {
            stretchOffset = 0;
        }

        float knobX = knobBaseX + (travelDistance * checkAnimation) - (stretchExtra * checkAnimation) + stretchOffset;
        float knobY = checkboxY + (checkboxSize - knobHeight) / 2f;

        int offGray = 59;
        int onGray = 159;
        int gray = (int)(offGray + (onGray - offGray) * checkAnimation);
        int knobColor = Theme.withAlpha(0xFF000000 | (gray << 16) | (gray << 8) | gray, (int)(200 * alphaMultiplier));
        RenderHelper.rect(g, knobX, knobY, knobWidth, knobHeight, knobColor);
    }

    private float clamp(float value, float min, float max) {
        return Math.max(min, Math.min(max, value));
    }

    private int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHover(mouseX, mouseY) && button == 0) {
            setter.accept(!getter.get());
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
