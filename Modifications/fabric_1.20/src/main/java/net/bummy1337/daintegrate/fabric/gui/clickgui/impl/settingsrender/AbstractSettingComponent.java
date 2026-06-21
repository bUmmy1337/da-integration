package net.bummy1337.daintegrate.fabric.gui.clickgui.impl.settingsrender;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public abstract class AbstractSettingComponent {
    protected float x, y, width, height;
    protected float alphaMultiplier = 1f;
    protected String name;
    protected String description;

    public AbstractSettingComponent(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void position(float x, float y) { this.x = x; this.y = y; }
    public void size(float w, float h) { this.width = w; this.height = h; }
    public void setAlphaMultiplier(float a) { this.alphaMultiplier = a; }

    public String getName() { return name; }
    public String getDescription() { return description; }

    public abstract void render(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta);
    public abstract boolean mouseClicked(double mouseX, double mouseY, int button);
    public boolean mouseReleased(double mouseX, double mouseY, int button) { return false; }
    public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) { return false; }
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }
    public boolean charTyped(char chr, int modifiers) { return false; }
    public void tick() {}
    public boolean isHover(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }
}
