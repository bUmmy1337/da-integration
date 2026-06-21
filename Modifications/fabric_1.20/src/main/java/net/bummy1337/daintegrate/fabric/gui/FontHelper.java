package net.bummy1337.daintegrate.fabric.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.FormattedCharSequence;

public class FontHelper {
    public static final Identifier FONT_ID =
            Identifier.fromNamespaceAndPath("daintegratew", "montserrat");

    private static final FontDescription FONT_DESC = new FontDescription.Resource(FONT_ID);

    private static boolean fontAvailable = false;

    public static void setFontAvailable(boolean value) {
        fontAvailable = value;
    }

    public static boolean isFontAvailable() {
        return fontAvailable;
    }

    public static Component comp(String text) {
        String safe = text != null ? text : "";
        if (!fontAvailable)
            return Component.literal(safe);
        return Component.literal(safe).withStyle(Style.EMPTY.withFont(FONT_DESC));
    }

    public static Component comp(String text, int color) {
        String safe = text != null ? text : "";
        if (!fontAvailable)
            return Component.literal(safe).withStyle(Style.EMPTY.withColor(color));
        return Component.literal(safe).withStyle(Style.EMPTY.withFont(FONT_DESC).withColor(color));
    }

    public static FormattedCharSequence seq(String text) {
        String safe = text != null ? text : "";
        if (!fontAvailable)
            return FormattedCharSequence.forward(safe, Style.EMPTY);
        return FormattedCharSequence.forward(safe, Style.EMPTY.withFont(FONT_DESC));
    }

    public static int width(Font font, String text) {
        if (text == null) return 0;
        if (!fontAvailable)
            return font.width(text);
        return font.width(comp(text));
    }
}
