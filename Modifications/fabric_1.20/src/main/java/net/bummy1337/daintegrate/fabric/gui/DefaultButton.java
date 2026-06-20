package net.bummy1337.daintegrate.fabric.gui;

public class DefaultButton extends CustomButton {

    public DefaultButton(int x, int y, int widthIn, int heightIn, String buttonText, Runnable onPress) {
        super(x, y, widthIn, heightIn, buttonText, onPress);
        super.DefaultBackgroundColor = Palette.BLACK_TRANSPARENT_xB0;
        super.HoveredBackgroundColor = Palette.YELLOW_TRANSPARENT_xA0;
        super.HoveredForegroundColor = Palette.WHITE;
    }

    public DefaultButton(int x, int y, int widthIn, boolean visibility, String buttonText, Runnable onPress) {
        this(x, y, widthIn, 20, buttonText, onPress);
        visible = visibility;
    }
}
