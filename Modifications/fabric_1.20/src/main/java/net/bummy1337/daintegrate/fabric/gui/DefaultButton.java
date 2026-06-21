package net.bummy1337.daintegrate.fabric.gui;

public class DefaultButton extends CustomButton {

    public DefaultButton(int x, int y, int widthIn, int heightIn, String buttonText, Runnable onPress) {
        super(x, y, widthIn, heightIn, buttonText, onPress);
        super.DefaultBackgroundColor = Theme.ACCENT;
        super.HoveredBackgroundColor = Theme.ACCENT_HOVER;
        super.HoveredForegroundColor = Theme.WHITE;
        super.DefaultForegroundColor = Theme.WHITE;
        super.OutlineColor = Theme.ACCENT_HOVER;
        super.OutlineHoverColor = Theme.WHITE;
    }

    public DefaultButton(int x, int y, int widthIn, boolean visibility, String buttonText, Runnable onPress) {
        this(x, y, widthIn, 20, buttonText, onPress);
        visible = visibility;
    }
}
