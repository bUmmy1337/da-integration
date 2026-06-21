package net.bummy1337.daintegrate.fabric.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public class CheckBox extends CustomButton {
    public boolean Flag;

    public CheckBox(int x, int y, int widthIn, boolean visibility, String buttonText, boolean flag, Runnable onPress) {
        this(x, y, widthIn, 20, buttonText, flag, onPress);
        visible = visibility;
    }

    public CheckBox(int x, int y, int widthIn, int heightIn, String buttonText, boolean flag, Runnable onPress) {
        super(x, y, widthIn, heightIn, buttonText, onPress);
        Flag = flag;
        ShowOutline = false;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            Font fontrenderer = Minecraft.getInstance().font;
            this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

            int switchW = 28;
            int switchH = 14;
            int switchX = getX();
            int switchY = getY() + (height - switchH) / 2;

            int trackColor = Flag ? Theme.ACCENT : Theme.BG_INPUT;
            if (this.isHovered && active)
                trackColor = Flag ? Theme.ACCENT_HOVER : Theme.BG_ENTRY_HOVER;

            graphics.fill(switchX, switchY, switchX + switchW, switchY + switchH, trackColor);
            graphics.outline(switchX, switchY, switchW, switchH, Flag ? Theme.ACCENT_HOVER : Theme.BORDER);

            int knobSize = 10;
            int knobX = Flag ? switchX + switchW - knobSize - 2 : switchX + 2;
            int knobY = switchY + (switchH - knobSize) / 2;
            graphics.fill(knobX, knobY, knobX + knobSize, knobY + knobSize, Theme.WHITE);

            Component message = this.getMessage();
            graphics.text(fontrenderer, FontHelper.comp(message.getString()),
                    getX() + switchW + 8, getY() + (height / 2) - 4,
                    Flag ? Theme.TEXT_PRIMARY : Theme.TEXT_SECONDARY, false);
        }
    }

    public void SwitchFlag(boolean value) {
        Flag = value;
    }
}
