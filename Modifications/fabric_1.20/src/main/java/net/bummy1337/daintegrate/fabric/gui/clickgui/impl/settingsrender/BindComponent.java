package net.bummy1337.daintegrate.fabric.gui.clickgui.impl.settingsrender;

import net.bummy1337.daintegrate.fabric.gui.Theme;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.RenderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

public class BindComponent extends AbstractSettingComponent {
    private final String settingName;
    private final String settingDescription;
    private final java.util.function.Supplier<Integer> keyGetter;
    private final java.util.function.Consumer<Integer> keySetter;
    private final java.util.function.Supplier<Integer> typeGetter;
    private final java.util.function.Consumer<Integer> typeSetter;

    private boolean listening = false;
    private float listeningAnimation = 0f;
    private float hoverAnimation = 0f;
    private float bindHoverAnimation = 0f;
    private float pulseAnimation = 0f;
    private float scaleAnimation = 1f;
    private float glowAnimation = 0f;
    private float textChangeAnimation = 0f;
    private String previousBindText = "";
    private String currentBindText = "";

    private long lastUpdateTime = System.currentTimeMillis();

    private static final float ANIMATION_SPEED = 8f;
    private static final float FAST_ANIMATION_SPEED = 12f;
    private static final float BIND_BOX_WIDTH = 32f;
    private static final float BIND_BOX_HEIGHT = 10f;

    public static final int SCROLL_UP_BIND = 1000;
    public static final int SCROLL_DOWN_BIND = 1001;
    public static final int MIDDLE_MOUSE_BIND = 1002;

    public BindComponent(String name, String description,
                         java.util.function.Supplier<Integer> keyGetter,
                         java.util.function.Consumer<Integer> keySetter,
                         java.util.function.Supplier<Integer> typeGetter,
                         java.util.function.Consumer<Integer> typeSetter) {
        super(name, description);
        this.settingName = name;
        this.settingDescription = description;
        this.keyGetter = keyGetter;
        this.keySetter = keySetter;
        this.typeGetter = typeGetter;
        this.typeSetter = typeSetter;
        this.currentBindText = getBindDisplayName(keyGetter.get(), typeGetter.get());
        this.previousBindText = this.currentBindText;
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

    @Override
    public void render(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        float deltaTime = getDeltaTime();

        boolean hovered = isHover(mouseX, mouseY);
        boolean bindHovered = isBindHover(mouseX, mouseY);

        hoverAnimation = lerp(hoverAnimation, hovered ? 1f : 0f, deltaTime * ANIMATION_SPEED);
        bindHoverAnimation = lerp(bindHoverAnimation, bindHovered ? 1f : 0f, deltaTime * ANIMATION_SPEED);
        listeningAnimation = lerp(listeningAnimation, listening ? 1f : 0f, deltaTime * FAST_ANIMATION_SPEED);

        float scaleTarget = listening ? 1.05f : (bindHovered ? 1.02f : 1f);
        scaleAnimation = lerp(scaleAnimation, scaleTarget, deltaTime * ANIMATION_SPEED);

        glowAnimation = lerp(glowAnimation, listening ? 1f : 0f, deltaTime * ANIMATION_SPEED);

        if (listening) {
            pulseAnimation += deltaTime * 4f;
            if (pulseAnimation > Math.PI * 2) {
                pulseAnimation -= (float)(Math.PI * 2);
            }
        } else {
            pulseAnimation = lerp(pulseAnimation, 0f, deltaTime * ANIMATION_SPEED);
        }

        String newBindText = listening ? "" : getBindDisplayName(keyGetter.get(), typeGetter.get());

        if (!newBindText.equals(currentBindText)) {
            previousBindText = currentBindText;
            currentBindText = newBindText;
            textChangeAnimation = 0f;
        }

        textChangeAnimation = lerp(textChangeAnimation, 1f, deltaTime * FAST_ANIMATION_SPEED);

        RenderHelper.text(g, settingName, x + 9.5f, y + height / 2 - 7.5f, 6,
                Theme.withAlpha(Theme.TEXT_PRIMARY, (int)(200 * alphaMultiplier)));

        if (settingDescription != null && !settingDescription.isEmpty()) {
            RenderHelper.text(g, settingDescription, x + 0.5f, y + height / 2 + 0.5f, 5,
                    Theme.withAlpha(Theme.TEXT_MUTED, (int)(128 * alphaMultiplier)));
        }

        renderBindBox(g, mouseX, mouseY);
    }

    private void renderBindBox(GuiGraphicsExtractor g, int mouseX, int mouseY) {
        float bindBoxX = x + width - BIND_BOX_WIDTH - 2;
        float bindBoxY = y + height / 2 - BIND_BOX_HEIGHT / 2;

        float scaledWidth = BIND_BOX_WIDTH * scaleAnimation;
        float scaledHeight = BIND_BOX_HEIGHT * scaleAnimation;
        float scaledX = bindBoxX - (scaledWidth - BIND_BOX_WIDTH) / 2;
        float scaledY = bindBoxY - (scaledHeight - BIND_BOX_HEIGHT) / 2;

        int bgAlpha = (int)(25 + bindHoverAnimation * 15 + listeningAnimation * 20);
        int bgColor;
        if (listening) {
            float pulse = (float)(Math.sin(pulseAnimation) * 0.15 + 0.85);
            bgColor = Theme.withAlpha(Theme.ACCENT, (int)(bgAlpha * alphaMultiplier * pulse));
        } else if (keyGetter.get() != GLFW.GLFW_KEY_UNKNOWN && keyGetter.get() != -1) {
            bgColor = Theme.withAlpha(Theme.GREEN, (int)(bgAlpha * alphaMultiplier));
        } else {
            bgColor = Theme.withAlpha(Theme.BG_INPUT, (int)(bgAlpha * alphaMultiplier));
        }

        RenderHelper.rect(g, scaledX, scaledY, scaledWidth, scaledHeight, bgColor);

        int outlineColor;
        if (listening) {
            float pulse = (float)(Math.sin(pulseAnimation) * 0.3 + 0.7);
            outlineColor = Theme.withAlpha(Theme.ACCENT, (int)(150 * pulse * listeningAnimation * alphaMultiplier));
        } else if (keyGetter.get() != GLFW.GLFW_KEY_UNKNOWN && keyGetter.get() != -1) {
            outlineColor = Theme.withAlpha(Theme.GREEN, (int)((80 + bindHoverAnimation * 40) * alphaMultiplier));
        } else {
            outlineColor = Theme.withAlpha(Theme.BORDER, (int)((60 + bindHoverAnimation * 40) * alphaMultiplier));
        }

        RenderHelper.outline(g, scaledX, scaledY, scaledWidth, scaledHeight, 0.5f, outlineColor);

        renderBindText(g, scaledX, scaledY, scaledWidth, scaledHeight);

        if (listening) {
            renderListeningIndicator(g, scaledX, scaledY, scaledWidth, scaledHeight);
        }
    }

    private void renderBindText(GuiGraphicsExtractor g, float boxX, float boxY, float boxWidth, float boxHeight) {
        var font = Minecraft.getInstance().font;
        float textY = boxY + boxHeight / 2 - 2.5f;
        float centerX = boxX + boxWidth / 2;

        int textColor;
        if (listening) {
            float pulse = (float)(Math.sin(pulseAnimation * 2) * 0.2 + 0.8);
            textColor = Theme.withAlpha(Theme.TEXT_ACCENT, (int)(220 * pulse * alphaMultiplier));
        } else if (keyGetter.get() != GLFW.GLFW_KEY_UNKNOWN && keyGetter.get() != -1) {
            textColor = Theme.withAlpha(Theme.GREEN, (int)(200 * alphaMultiplier));
        } else {
            textColor = Theme.withAlpha(Theme.TEXT_MUTED, (int)(150 * alphaMultiplier));
        }

        if (textChangeAnimation < 1f && !previousBindText.equals(currentBindText)) {
            float oldAlpha = 1f - textChangeAnimation;
            float newAlpha = textChangeAnimation;

            float oldOffsetY = -3f * textChangeAnimation;
            float newOffsetY = 3f * (1f - textChangeAnimation);

            if (oldAlpha > 0.01f) {
                int oldColor = Theme.withAlpha(textColor, (int)(oldAlpha * 255));
                int w = net.bummy1337.daintegrate.fabric.gui.FontHelper.width(font, previousBindText);
                RenderHelper.text(g, previousBindText, centerX - w / 2f, textY + oldOffsetY, 5, oldColor);
            }

            int newColor = Theme.withAlpha(textColor, (int)(newAlpha * 255));
            int w = net.bummy1337.daintegrate.fabric.gui.FontHelper.width(font, currentBindText);
            RenderHelper.text(g, currentBindText, centerX - w / 2f, textY + newOffsetY, 5, newColor);
        } else {
            int w = net.bummy1337.daintegrate.fabric.gui.FontHelper.width(font, currentBindText);
            RenderHelper.text(g, currentBindText, centerX - w / 2f, textY, 5, textColor);
        }
    }

    private void renderListeningIndicator(GuiGraphicsExtractor g, float boxX, float boxY,
                                          float boxWidth, float boxHeight) {
        float dotSpacing = 3f;
        float dotSize = 1.5f;
        float dotsWidth = dotSpacing * 2;
        float startX = boxX + (boxWidth - dotsWidth) / 2 - dotSize / 2;
        float dotY = boxY + boxHeight - 5.5f;

        for (int i = 0; i < 3; i++) {
            float phase = pulseAnimation + i * 0.5f;
            float pulse = (float)(Math.sin(phase * 2) * 0.5 + 0.5);
            float currentDotSize = dotSize * (0.5f + pulse * 0.5f);

            int alpha = (int)(150 * (0.3f + pulse * 0.7f) * listeningAnimation * alphaMultiplier);

            float dotX = startX + i * dotSpacing + (dotSize - currentDotSize) / 2;
            float adjustedDotY = dotY + (dotSize - currentDotSize) / 2;

            RenderHelper.rect(g, dotX, adjustedDotY, currentDotSize, currentDotSize,
                    Theme.withAlpha(Theme.ACCENT, alpha));
        }
    }

    private String getBindDisplayName(int key, int type) {
        if (key == GLFW.GLFW_KEY_UNKNOWN || key == -1) return "None";

        if (key == SCROLL_UP_BIND) return "ScrollUp";
        if (key == SCROLL_DOWN_BIND) return "ScrollDn";
        if (key == MIDDLE_MOUSE_BIND) return "MMB";

        if (type == 0) {
            return switch (key) {
                case GLFW.GLFW_MOUSE_BUTTON_LEFT -> "LMB";
                case GLFW.GLFW_MOUSE_BUTTON_RIGHT -> "RMB";
                case GLFW.GLFW_MOUSE_BUTTON_MIDDLE -> "MMB";
                case GLFW.GLFW_MOUSE_BUTTON_4 -> "M4";
                case GLFW.GLFW_MOUSE_BUTTON_5 -> "M5";
                case GLFW.GLFW_MOUSE_BUTTON_6 -> "M6";
                case GLFW.GLFW_MOUSE_BUTTON_7 -> "M7";
                case GLFW.GLFW_MOUSE_BUTTON_8 -> "M8";
                default -> "M" + key;
            };
        }

        String keyName = GLFW.glfwGetKeyName(key, 0);
        if (keyName == null) {
            return switch (key) {
                case GLFW.GLFW_KEY_LEFT_SHIFT -> "LShift";
                case GLFW.GLFW_KEY_RIGHT_SHIFT -> "RShift";
                case GLFW.GLFW_KEY_LEFT_CONTROL -> "LCtrl";
                case GLFW.GLFW_KEY_RIGHT_CONTROL -> "RCtrl";
                case GLFW.GLFW_KEY_LEFT_ALT -> "LAlt";
                case GLFW.GLFW_KEY_RIGHT_ALT -> "RAlt";
                case GLFW.GLFW_KEY_SPACE -> "Space";
                case GLFW.GLFW_KEY_TAB -> "Tab";
                case GLFW.GLFW_KEY_CAPS_LOCK -> "Caps";
                case GLFW.GLFW_KEY_ENTER -> "Enter";
                case GLFW.GLFW_KEY_BACKSPACE -> "Back";
                case GLFW.GLFW_KEY_INSERT -> "Ins";
                case GLFW.GLFW_KEY_DELETE -> "Del";
                case GLFW.GLFW_KEY_HOME -> "Home";
                case GLFW.GLFW_KEY_END -> "End";
                case GLFW.GLFW_KEY_PAGE_UP -> "PgUp";
                case GLFW.GLFW_KEY_PAGE_DOWN -> "PgDn";
                case GLFW.GLFW_KEY_UP -> "Up";
                case GLFW.GLFW_KEY_DOWN -> "Down";
                case GLFW.GLFW_KEY_LEFT -> "Left";
                case GLFW.GLFW_KEY_RIGHT -> "Right";
                case GLFW.GLFW_KEY_F1 -> "F1";
                case GLFW.GLFW_KEY_F2 -> "F2";
                case GLFW.GLFW_KEY_F3 -> "F3";
                case GLFW.GLFW_KEY_F4 -> "F4";
                case GLFW.GLFW_KEY_F5 -> "F5";
                case GLFW.GLFW_KEY_F6 -> "F6";
                case GLFW.GLFW_KEY_F7 -> "F7";
                case GLFW.GLFW_KEY_F8 -> "F8";
                case GLFW.GLFW_KEY_F9 -> "F9";
                case GLFW.GLFW_KEY_F10 -> "F10";
                case GLFW.GLFW_KEY_F11 -> "F11";
                case GLFW.GLFW_KEY_F12 -> "F12";
                case GLFW.GLFW_KEY_ESCAPE -> "Esc";
                case GLFW.GLFW_KEY_PRINT_SCREEN -> "Print";
                case GLFW.GLFW_KEY_SCROLL_LOCK -> "Scroll";
                case GLFW.GLFW_KEY_PAUSE -> "Pause";
                case GLFW.GLFW_KEY_NUM_LOCK -> "NumLk";
                case GLFW.GLFW_KEY_KP_0 -> "Num0";
                case GLFW.GLFW_KEY_KP_1 -> "Num1";
                case GLFW.GLFW_KEY_KP_2 -> "Num2";
                case GLFW.GLFW_KEY_KP_3 -> "Num3";
                case GLFW.GLFW_KEY_KP_4 -> "Num4";
                case GLFW.GLFW_KEY_KP_5 -> "Num5";
                case GLFW.GLFW_KEY_KP_6 -> "Num6";
                case GLFW.GLFW_KEY_KP_7 -> "Num7";
                case GLFW.GLFW_KEY_KP_8 -> "Num8";
                case GLFW.GLFW_KEY_KP_9 -> "Num9";
                case GLFW.GLFW_KEY_KP_DECIMAL -> "Num.";
                case GLFW.GLFW_KEY_KP_DIVIDE -> "Num/";
                case GLFW.GLFW_KEY_KP_MULTIPLY -> "Num*";
                case GLFW.GLFW_KEY_KP_SUBTRACT -> "Num-";
                case GLFW.GLFW_KEY_KP_ADD -> "Num+";
                case GLFW.GLFW_KEY_KP_ENTER -> "NumEnt";
                default -> "Key" + key;
            };
        }
        return keyName.toUpperCase();
    }

    private boolean isBindHover(double mouseX, double mouseY) {
        float bindBoxX = x + width - BIND_BOX_WIDTH - 2;
        float bindBoxY = y + height / 2 - BIND_BOX_HEIGHT / 2;
        return mouseX >= bindBoxX && mouseX <= bindBoxX + BIND_BOX_WIDTH &&
                mouseY >= bindBoxY && mouseY <= bindBoxY + BIND_BOX_HEIGHT;
    }

    public void handleScrollBind(double vertical) {
        if (listening) {
            if (vertical > 0) {
                keySetter.accept(SCROLL_UP_BIND);
            } else {
                keySetter.accept(SCROLL_DOWN_BIND);
            }
            typeSetter.accept(2);
            listening = false;
        }
    }

    public void handleMiddleMouseBind() {
        if (listening) {
            keySetter.accept(MIDDLE_MOUSE_BIND);
            typeSetter.accept(2);
            listening = false;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isBindHover(mouseX, mouseY)) {
            if (button == 1) {
                keySetter.accept(GLFW.GLFW_KEY_UNKNOWN);
                typeSetter.accept(1);
                listening = false;
                return true;
            } else if (listening) {
                keySetter.accept(button);
                typeSetter.accept(0);
                listening = false;
                return true;
            } else if (button == 0) {
                listening = true;
                return true;
            }
        } else if (listening) {
            listening = false;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (listening) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                listening = false;
                return true;
            } else if (keyCode == GLFW.GLFW_KEY_BACKSPACE || keyCode == GLFW.GLFW_KEY_DELETE) {
                keySetter.accept(GLFW.GLFW_KEY_UNKNOWN);
                typeSetter.accept(1);
                listening = false;
                return true;
            } else if (keyCode != GLFW.GLFW_KEY_UNKNOWN) {
                keySetter.accept(keyCode);
                typeSetter.accept(1);
                listening = false;
                return true;
            }
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

    public boolean isListening() {
        return listening;
    }
}
