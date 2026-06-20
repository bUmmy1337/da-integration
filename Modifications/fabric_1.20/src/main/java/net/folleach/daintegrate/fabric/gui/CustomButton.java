package net.folleach.daintegrate.fabric.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

public class CustomButton extends AbstractButton {
    public int DefaultBackgroundColor = 0x30000000;
    public int DefaultForegroundColor = 0xFFFFFFFF;
    public int HoveredBackgroundColor = 0x60000000;
    public int HoveredForegroundColor = 0xFFF59907;
    public int DisableBackgroundColor = DefaultBackgroundColor;
    public int DisableForegroundColor = 0xFFA0A0A0;
    protected final Runnable onClick;

    public CustomButton(int x, int y, int width, boolean visibility, String text, Runnable onClick) {
        super(x, y, width, 20, Component.literal(text));
        this.visible = visibility;
        this.onClick = onClick;
    }

    public CustomButton(int x, int y, int widthIn, int heightIn, String text, Runnable onClick) {
        super(x, y, widthIn, heightIn, Component.literal(text));
        this.onClick = onClick;
    }

    public void drawButton(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        extractWidgetRenderState(graphics, mouseX, mouseY, partialTicks);
    }

    public void drawButton(GuiGraphicsExtractor graphics, Minecraft mc, int x, int y, int mouseX, int mouseY, float partialTicks) {
        this.setX(x);
        this.setY(y);
        extractWidgetRenderState(graphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onPress(InputWithModifiers modifiers) {
        if (onClick != null) {
            onClick.run();
        }
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            Font fontrenderer = Minecraft.getInstance().font;
            this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
            int colorBg = DefaultBackgroundColor;
            int colorFg = DefaultForegroundColor;
            if (!this.active) {
                colorBg = DisableBackgroundColor;
                colorFg = DisableForegroundColor;
            } else if (this.isHovered) {
                colorBg = HoveredBackgroundColor;
                colorFg = HoveredForegroundColor;
            }
            graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, colorBg);
            Component message = this.getMessage();
            int textWidth = fontrenderer.width(message);
            graphics.text(fontrenderer, message, this.getX() + (this.width - textWidth) / 2, this.getY() + (this.height - 8) / 2, colorFg, false);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
    }
}
