package net.folleach.daintegrate.fabric.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;

public class CheckBox extends CustomButton {
    public boolean Flag;

    public CheckBox(int x, int y, int widthIn, boolean visibility, String buttonText, boolean flag, Runnable onPress) {
        this(x, y, widthIn, 20, buttonText, flag, onPress);
        Flag = flag;
    }

    public CheckBox(int x, int y, int widthIn, int heightIn, String buttonText, boolean flag, Runnable onPress) {
        super(x, y, widthIn, heightIn, buttonText, onPress);
        Flag = flag;
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
            }
            graphics.fill(getX(), getY(), getX() + width, getY() + height, colorBg);
            int colorFlag = Palette.RED;
            if (Flag)
                colorFlag = Palette.GREEN;
            graphics.fill(getX() + 4, getY() + 4, getX() + 16, getY() + 16, colorFlag);
            Component message = this.getMessage();
            graphics.text(fontrenderer, message, getX() + 22, getY() + (height / 2) - 4, colorFg, false);
        }
    }

    public void SwitchFlag(boolean value) {
        Flag = value;
    }
}
