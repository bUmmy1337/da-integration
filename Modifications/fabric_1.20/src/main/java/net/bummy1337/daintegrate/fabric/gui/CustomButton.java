package net.bummy1337.daintegrate.fabric.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class CustomButton extends AbstractWidget {
    public int DefaultBackgroundColor = Theme.BG_ENTRY;
    public int HoveredBackgroundColor = Theme.BG_ENTRY_HOVER;
    public int DefaultForegroundColor = Theme.TEXT_PRIMARY;
    public int HoveredForegroundColor = Theme.TEXT_PRIMARY;
    public int DisableBackgroundColor = DefaultBackgroundColor;
    public int DisableForegroundColor = Theme.TEXT_MUTED;
    public int OutlineColor = Theme.BORDER;
    public int OutlineHoverColor = Theme.BORDER_HOVER;
    public boolean UseGradient = false;
    public boolean ShowOutline = true;
    public int CornerRadius = 0;
    public boolean CircleMode = false;
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
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (this.active && this.visible && event.input() == 0) {
            if (event.x() >= this.getX() && event.y() >= this.getY()
                    && event.x() < this.getX() + this.width && event.y() < this.getY() + this.height) {
                playButtonClickSound(Minecraft.getInstance().getSoundManager());
                if (onClick != null)
                    onClick.run();
                return true;
            }
        }
        return false;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        renderContents(graphics, mouseX, mouseY, delta);
    }

    private void renderContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        if (this.visible) {
            Font fontrenderer = Minecraft.getInstance().font;
            this.isHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
            int colorBg = DefaultBackgroundColor;
            int colorFg = DefaultForegroundColor;
            int colorOutline = OutlineColor;
            if (!this.active) {
                colorBg = DisableBackgroundColor;
                colorFg = DisableForegroundColor;
                colorOutline = Theme.BORDER;
            } else if (this.isHovered) {
                colorBg = HoveredBackgroundColor;
                colorFg = HoveredForegroundColor;
                colorOutline = OutlineHoverColor;
            }
            if (UseGradient) {
                graphics.fillGradient(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, colorBg, Theme.BG_MAIN);
            } else if (CornerRadius > 0 || CircleMode) {
                int radius = CircleMode ? Math.min(this.width, this.height) / 2 : CornerRadius;
                Theme.fillRounded(graphics, this.getX(), this.getY(), this.width, this.height, radius, colorBg);
            } else {
                graphics.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, colorBg);
            }
            if (ShowOutline) {
                graphics.outline(this.getX(), this.getY(), this.width, this.height, colorOutline);
            }
            Component message = this.getMessage();
            String textStr = message.getString();
            int textWidth = FontHelper.width(fontrenderer, textStr);
            graphics.text(fontrenderer, FontHelper.comp(textStr),
                    this.getX() + (this.width - textWidth) / 2,
                    this.getY() + (this.height - 8) / 2,
                    colorFg, false);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
    }
}
