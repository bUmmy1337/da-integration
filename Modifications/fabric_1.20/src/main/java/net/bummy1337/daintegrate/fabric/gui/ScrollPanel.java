package net.bummy1337.daintegrate.fabric.gui;

import com.google.common.collect.Lists;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.util.List;

public class ScrollPanel<T extends IEntry> extends AbstractWidget {
    private final List<T> entries = Lists.<T>newArrayList();

    private int x, y, width, height;
    public int scrollPosition;
    public int scrollbarHeight;
    public int visualHeight;
    public int contentHeight;
    public boolean visible;

    public ScrollPanel(int x, int y, int width, int height) {
        super(x, y, width, height, Component.literal("msg"));
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.visualHeight = height - y;
    }

    public void drawPanel(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTicks) {
        if (-scrollPosition > contentHeight - visualHeight)
            scrollPosition = -(contentHeight - visualHeight);

        if (scrollPosition > 0)
            scrollPosition = 0;

        if (contentHeight > visualHeight) {
            int pos = convertRange(-scrollPosition, 0, contentHeight - visualHeight, 0, height - 50);
            graphics.fill(width - 4, y + pos, width, pos + 50, Palette.YELLOW);
        }
        int offset = scrollPosition + y;
        for (int i = 0; i < entries.size(); i++) {
            entries.get(i).drawEntry(graphics, x, offset, mouseX, mouseY, partialTicks);
            offset += entries.get(i).getHeightE();
        }
    }

    private static int convertRange(int value, int fromLow, int fromHigh, int toLow, int toHigh) {
        return toLow + (value - fromLow) * (toHigh - toLow) / (fromHigh - fromLow);
    }

    public boolean charTyped(CharacterEvent event) {
        for (T entry : entries)
            entry.charTyped(event);
        return true;
    }

    public boolean keyPressed(KeyEvent event) {
        for (T entry : entries)
            entry.keyPressed(event);
        return true;
    }

    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.y() >= 20)
            for (T entry : entries)
                entry.mouseClicked(event, doubleClick);
        return true;
    }

    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        scrollPosition += delta * 30;
        return true;
    }

    public void updateHeight() {
        contentHeight = 0;
        for (int i = 0; i < entries.size(); i++)
            contentHeight += entries.get(i).getHeightE();
    }

    public void addEntry(T entry) {
        contentHeight += entry.getHeightE();
        entries.add(entry);
    }

    public List<T> getEntries() {
        return entries;
    }

    public void clearEntries() {
        contentHeight = 0;
        this.entries.clear();
    }

    public void removeAt(int index) {
        entries.remove(index);
        updateHeight();
    }

    public void removeEntry(T entry) {
        entries.remove(entry);
        updateHeight();
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput builder) {
    }
}
