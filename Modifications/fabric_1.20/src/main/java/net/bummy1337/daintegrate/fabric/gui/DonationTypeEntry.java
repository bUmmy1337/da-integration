package net.bummy1337.daintegrate.fabric.gui;

import net.bummy1337.daintegrate.configurations.HandlerPropertiesDto;
import net.bummy1337.daintegrate.configurations.PropertiesDto;
import net.bummy1337.daintegrate.configurations.SensitivePropertiesDto;
import net.bummy1337.daintegrate.configurations.TriggerDto;
import net.bummy1337.daintegrate.handlers.CommandHandlerProperties;
import net.bummy1337.daintegrate.handlers.MessageHandlerProperties;
import net.bummy1337.daintegrate.sensitives.DonateSensitiveProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.ArrayList;
import java.util.List;

public class DonationTypeEntry implements IEntry {
    private static final String langMessages = "Messages";
    private static final String langCommands = "Commands";
    private static final String langLine = "Line";
    private static final String langName = "Name";
    private static final String langActive = "Active";

    private final ScrollPanel owner;
    public final TriggerDto trigger;
    private final Font fontRenderer;
    private final Minecraft mc;
    private int right;

    private CustomTextBox textBoxName;
    private CheckBox checkBoxActive;
    private CustomTextBox textBoxFrom;
    private CustomTextBox textBoxTo;
    private CustomTextBox textBoxCurrency;

    private List<WritableLineElement> messages;
    private List<WritableLineElement> commands;
    private CustomButton addMessage;
    private CustomButton addCommand;
    private CustomButton deleteEntry;

    public boolean hasError = false;

    public DonationTypeEntry(ScrollPanel own, TriggerDto trigger, Minecraft mc, int right) {
        this.right = right;
        owner = own;
        messages = new ArrayList<>();
        commands = new ArrayList<>();

        addMessage = new CustomButton(0, 0, 20, true, "+", this::addMessageClick);
        addMessage.DefaultBackgroundColor = Palette.GREEN;
        addMessage.HoveredBackgroundColor = Palette.GREEN_HOVERED;
        addMessage.HoveredForegroundColor = Palette.WHITE;
        addCommand = new CustomButton(0, 0, 20, true, "+", this::addCommandClick);
        addCommand.DefaultBackgroundColor = Palette.GREEN;
        addCommand.HoveredBackgroundColor = Palette.GREEN_HOVERED;
        addCommand.HoveredForegroundColor = Palette.WHITE;

        this.trigger = trigger;
        this.fontRenderer = mc.font;
        this.mc = mc;
        textBoxName = new CustomTextBox(fontRenderer, 0, 0, 200, 20, "");
        textBoxName.tag = langName;
        textBoxName.setText(trigger.name != null ? trigger.name : "");
        checkBoxActive = new CheckBox(0, 0, 60, true, langActive, trigger.isActive, this::checkBoxClick);

        deleteEntry = new CustomButton(0, 0, 20, true, "-", this::deleteEntryClick);
        deleteEntry.DefaultBackgroundColor = Palette.RED;
        deleteEntry.HoveredBackgroundColor = Palette.RED_HOVERED;
        deleteEntry.HoveredForegroundColor = Palette.WHITE;

        textBoxFrom = new CustomTextBox(fontRenderer, 0, 0, 80, 20, "0");
        textBoxFrom.tag = "From";
        textBoxTo = new CustomTextBox(fontRenderer, 0, 0, 80, 20, "999999");
        textBoxTo.tag = "To";
        textBoxCurrency = new CustomTextBox(fontRenderer, 0, 0, 80, 20, "");
        textBoxCurrency.tag = "Currency";

        if (trigger.sensitives != null) {
            for (var sensitive : trigger.sensitives) {
                if (sensitive.properties == null || sensitive.properties.type == null)
                    continue;
                var type = sensitive.properties.type;
                var shortType = type.contains("/") ? type.substring(type.lastIndexOf("/") + 1) : type;
                if ("donate".equals(shortType) && sensitive.properties.value instanceof DonateSensitiveProperties) {
                    var dp = (DonateSensitiveProperties) sensitive.properties.value;
                    textBoxFrom.setText(String.valueOf(dp.from));
                    textBoxTo.setText(String.valueOf(dp.to));
                    textBoxCurrency.setText(dp.currency != null ? dp.currency : "");
                }
            }
        }

        if (trigger.handlers != null) {
            for (int i = 0; i < trigger.handlers.length; i++) {
                var handler = trigger.handlers[i];
                if (handler.properties == null || handler.properties.type == null)
                    continue;
                var type = handler.properties.type;
                var shortType = type.contains("/") ? type.substring(type.lastIndexOf("/") + 1) : type;
                if ("message".equals(shortType)) {
                    var line = new WritableLineElement(this, mc, WritableType.Message, langLine + " " + (messages.size() + 1));
                    if (handler.properties.value instanceof MessageHandlerProperties)
                        line.line.setText(((MessageHandlerProperties) handler.properties.value).message);
                    else if (handler.properties.value instanceof String)
                        line.line.setText((String) handler.properties.value);
                    messages.add(line);
                } else if ("command".equals(shortType)) {
                    var line = new WritableLineElement(this, mc, WritableType.Command, langLine + " " + (commands.size() + 1));
                    if (handler.properties.value instanceof CommandHandlerProperties)
                        line.line.setText(((CommandHandlerProperties) handler.properties.value).command);
                    else if (handler.properties.value instanceof String)
                        line.line.setText((String) handler.properties.value);
                    commands.add(line);
                }
            }
        }
    }

    private void addMessageClick() {
        messages.add(new WritableLineElement(this, mc, WritableType.Message, langLine + " " + (messages.size() + 1)));
        owner.updateHeight();
    }

    private void addCommandClick() {
        commands.add(new WritableLineElement(this, mc, WritableType.Command, langLine + " " + (commands.size() + 1)));
        owner.updateHeight();
    }

    private void deleteEntryClick() {
        owner.removeEntry(this);
    }

    private void checkBoxClick() {
        checkBoxActive.SwitchFlag(!checkBoxActive.Flag);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        textBoxName.mouseClicked(event, doubleClick);
        checkBoxActive.mouseClicked(event, doubleClick);
        addMessage.mouseClicked(event, doubleClick);
        addCommand.mouseClicked(event, doubleClick);
        deleteEntry.mouseClicked(event, doubleClick);
        textBoxFrom.mouseClicked(event, doubleClick);
        textBoxTo.mouseClicked(event, doubleClick);
        textBoxCurrency.mouseClicked(event, doubleClick);
        for (var msg : messages) {
            msg.line.mouseClicked(event, doubleClick);
            msg.delete.mouseClicked(event, doubleClick);
        }
        for (var cmd : commands) {
            cmd.line.mouseClicked(event, doubleClick);
            cmd.delete.mouseClicked(event, doubleClick);
        }
        return true;
    }

    @Override
    public void charTyped(CharacterEvent event) {
        textBoxName.charTyped(event);
        textBoxFrom.charTyped(event);
        textBoxTo.charTyped(event);
        textBoxCurrency.charTyped(event);
        for (var msg : messages)
            msg.line.charTyped(event);
        for (var cmd : commands)
            cmd.line.charTyped(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        textBoxName.keyPressed(event);
        textBoxFrom.keyPressed(event);
        textBoxTo.keyPressed(event);
        textBoxCurrency.keyPressed(event);
        for (var msg : messages)
            msg.line.keyPressed(event);
        for (var cmd : commands)
            cmd.line.keyPressed(event);
        return true;
    }

    @Override
    public void drawEntry(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float partialTicks) {
        int offset = y;
        textBoxName.renderButton(graphics, x, offset, partialTicks);
        checkBoxActive.drawButton(graphics, mc, x + 210, y + 10, mouseX, mouseY, partialTicks);
        deleteEntry.drawButton(graphics, mc, x + 275, y + 10, mouseX, mouseY, partialTicks);
        offset += 35;
        textBoxFrom.renderButton(graphics, x, offset, partialTicks);
        textBoxTo.renderButton(graphics, x + 90, offset, partialTicks);
        textBoxCurrency.renderButton(graphics, x + 180, offset, partialTicks);
        offset += 35;
        graphics.text(fontRenderer, langMessages, x, offset, Palette.WHITE, true);
        offset += 10;
        for (int i = 0; i < messages.size(); i++) {
            messages.get(i).drawElement(graphics, x, offset, mouseX, mouseY, partialTicks);
            offset += 35;
        }
        addMessage.drawButton(graphics, mc, x + 210, offset, mouseX, mouseY, partialTicks);
        offset += 25;
        graphics.text(fontRenderer, langCommands, x, offset, Palette.WHITE, true);
        offset += 10;
        for (int i = 0; i < commands.size(); i++) {
            commands.get(i).drawElement(graphics, x, offset, mouseX, mouseY, partialTicks);
            offset += 35;
        }
        addCommand.drawButton(graphics, mc, x + 210, offset, mouseX, mouseY, partialTicks);
        offset += 25;
        graphics.fill(x, offset, right - 10, offset + 1, Palette.GRAY60);
        offset += 20;
    }

    @Override
    public int getHeightE() {
        return 155 + (messages.size() * 35) + (commands.size() * 35);
    }

    public void removeMessage(WritableLineElement ctb) {
        messages.remove(ctb);
        for (int i = 0; i < messages.size(); i++)
            messages.get(i).changeTag(langLine + " " + (i + 1));
    }

    public void removeCommands(WritableLineElement ctb) {
        commands.remove(ctb);
        for (int i = 0; i < commands.size(); i++)
            commands.get(i).changeTag(langLine + " " + (i + 1));
    }

    public String getName() {
        return textBoxName.getText();
    }

    public boolean getActive() {
        return checkBoxActive.Flag;
    }

    public List<String> getMessages() {
        List<String> msgs = new ArrayList<>();
        for (int i = 0; i < messages.size(); i++)
            msgs.add(messages.get(i).line.getText());
        return msgs;
    }

    public List<String> getCommands() {
        List<String> cmds = new ArrayList<>();
        for (int i = 0; i < commands.size(); i++)
            cmds.add(commands.get(i).line.getText());
        return cmds;
    }

    public String getFrom() {
        return textBoxFrom.getText();
    }

    public String getTo() {
        return textBoxTo.getText();
    }

    public String getCurrency() {
        return textBoxCurrency.getText();
    }

    enum WritableType {
        Message, Command
    }

    public class WritableLineElement {
        private final Minecraft mc;
        private final DonationTypeEntry callbackThinking;
        private final WritableType wtype;

        public CustomTextBox line;

        private CustomButton delete;

        public WritableLineElement(DonationTypeEntry dte, Minecraft mc, WritableType t, String tag) {
            this.mc = mc;
            callbackThinking = dte;
            wtype = t;
            line = new CustomTextBox(mc.font, 0, 0, 200, 20, "");
            line.tag = tag;
            delete = new CustomButton(0, 0, 20, true, "-", () -> deleteClick());
        }

        public void deleteClick() {
            if (wtype == WritableType.Message)
                callbackThinking.removeMessage(this);
            else if (wtype == WritableType.Command)
                callbackThinking.removeCommands(this);
            owner.updateHeight();
        }

        public void changeTag(String tag) {
            line.tag = tag;
        }

        public void drawElement(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float partialTicks) {
            line.renderButton(graphics, x, y, partialTicks);
            delete.drawButton(graphics, mc, x + 210, y + 10, mouseX, mouseY, partialTicks);
        }
    }
}
