package net.bummy1337.daintegrate.fabric.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

public class MessageEntry implements IEntry {
    private Font fontRenderer;
    private String title;
    private List<String> messageLines;
    private int width;

    public MessageEntry(Font fontR, String userName, String amount, String currency, String message, int width) {
        fontRenderer = fontR;
        this.width = width;
        title = userName + " - " + amount + " " + currency;
        messageLines = listFormattedStringToWidth(message, width);
    }

    private List<String> listFormattedStringToWidth(String value, int width) {
        List<String> result = new ArrayList<String>();
        result.add("");
        int spaceSize = fontRenderer.width(" ");
        String[] array = value.split("\\s+");
        int currentWidth = 0;
        for (int i = 0; i < array.length; i++) {
            int wordWidth = fontRenderer.width(array[i]);
            if (currentWidth + wordWidth + spaceSize > width) {
                result.add(array[i]);
                currentWidth = wordWidth + spaceSize;
                continue;
            }
            result.set(result.size() - 1, result.get(result.size() - 1) + " " + array[i]);
            currentWidth += wordWidth + spaceSize;
        }
        return result;
    }

    @Override
    public void drawEntry(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float partialTicks) {
        int offset = y;
        graphics.text(fontRenderer, title, x, offset, Palette.WHITE, true);
        offset += 10;
        for (String element : messageLines) {
            graphics.text(fontRenderer, element, x, offset, Palette.WHITE, true);
            offset += 10;
        }
        graphics.fill(x, offset, x + width, offset + 1, Palette.GRAY30);
        offset += 5;
    }

    @Override
    public int getHeightE() {
        return 15 + messageLines.size() * 10;
    }
}
