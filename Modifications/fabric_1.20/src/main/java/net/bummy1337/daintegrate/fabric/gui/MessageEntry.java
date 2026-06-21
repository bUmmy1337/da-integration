package net.bummy1337.daintegrate.fabric.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.ArrayList;
import java.util.List;

public class MessageEntry implements IEntry {
    private final Font fontRenderer;
    private final String userName;
    private final String amount;
    private final String currency;
    private final String message;
    private final int width;
    private List<String> messageLines;

    public MessageEntry(Font fontR, String userName, String amount, String currency, String message, int width) {
        fontRenderer = fontR;
        this.userName = userName;
        this.amount = amount;
        this.currency = currency;
        this.message = message;
        this.width = width;
        messageLines = wrapText(message, width - 20);
    }

    private List<String> wrapText(String value, int maxWidth) {
        List<String> result = new ArrayList<>();
        if (value == null || value.isEmpty()) {
            result.add("");
            return result;
        }
        int spaceSize = fontRenderer.width(" ");
        String[] array = value.split("\\s+");
        StringBuilder current = new StringBuilder();
        int currentWidth = 0;
        for (String word : array) {
            int wordWidth = FontHelper.width(fontRenderer, word);
            if (currentWidth + wordWidth + spaceSize > maxWidth && current.length() > 0) {
                result.add(current.toString());
                current = new StringBuilder(word);
                currentWidth = wordWidth + spaceSize;
            } else {
                if (current.length() > 0) {
                    current.append(" ");
                    currentWidth += spaceSize;
                }
                current.append(word);
                currentWidth += wordWidth;
            }
        }
        if (current.length() > 0)
            result.add(current.toString());
        return result;
    }

    @Override
    public void drawEntry(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float partialTicks) {
        int cardH = getHeightE() - 4;
        boolean hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + cardH;
        Theme.fillCard(graphics, x, y, width, cardH, hovered);

        Theme.drawLeftAccentBar(graphics, x, y, cardH, Theme.ACCENT);

        int innerX = x + 12;
        int offset = y + 5;

        graphics.text(fontRenderer, FontHelper.comp(userName), innerX, offset, Theme.TEXT_PRIMARY, false);
        String amtText = amount + " " + currency;
        int amtW = FontHelper.width(fontRenderer, amtText);
        graphics.text(fontRenderer, FontHelper.comp(amtText), x + width - amtW - 8, offset, Theme.YELLOW, false);
        offset += 14;

        for (String line : messageLines) {
            graphics.text(fontRenderer, FontHelper.comp(line), innerX, offset, Theme.TEXT_SECONDARY, false);
            offset += 10;
        }
    }

    @Override
    public int getHeightE() {
        return 10 + messageLines.size() * 10 + 10;
    }
}
