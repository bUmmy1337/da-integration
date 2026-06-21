package net.bummy1337.daintegrate.fabric.gui;

import net.bummy1337.daintegrate.configurations.HandlerPropertiesDto;
import net.bummy1337.daintegrate.configurations.PropertiesDto;
import net.bummy1337.daintegrate.configurations.SensitivePropertiesDto;
import net.bummy1337.daintegrate.configurations.TriggerDto;
import net.bummy1337.daintegrate.handlers.CommandHandlerProperties;
import net.bummy1337.daintegrate.handlers.MessageHandlerProperties;
import net.bummy1337.daintegrate.sensitives.DonateSensitiveProperties;
import net.bummy1337.daintegrate.sensitives.SubscribeSensitiveProperties;
import net.bummy1337.daintegrate.sensitives.TwitchBitsSensitiveProperties;
import net.bummy1337.daintegrate.sensitives.TwitchPointsSensitiveProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;

import java.util.ArrayList;
import java.util.List;

public class DonationTypeEntry implements IEntry {
    public enum TriggerKind {
        Donate, Subscribe, Twitch
    }

    private static final String[] SUBSCRIBE_TYPES = {
        "YouTubeSubscription", "TwitchSubscription", "TwitchFreeFollow",
        "TwitchGiftSubscription", "TwitchPrimeSubscription"
    };

    private static final String[] TWITCH_SUB_TYPES = { "Points", "Bits" };

    private final TriggerKind kind;
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

    private String subscribeType;
    private String twitchSubType;
    private CustomButton cycleLeft;
    private CustomButton cycleRight;

    private List<WritableLineElement> messages;
    private List<WritableLineElement> commands;
    private CustomButton addMessage;
    private CustomButton addCommand;
    private CustomButton deleteEntry;
    private boolean showActiveToggle = true;

    public boolean hasError = false;

    public DonationTypeEntry(ScrollPanel own, TriggerDto trigger, Minecraft mc, int right) {
        this(own, trigger, mc, right, detectKind(trigger));
    }

    public DonationTypeEntry(ScrollPanel own, TriggerDto trigger, Minecraft mc, int right, TriggerKind kind) {
        this.right = right;
        this.kind = kind;
        owner = own;
        messages = new ArrayList<>();
        commands = new ArrayList<>();

        addMessage = new CustomButton(0, 0, 14, 14, "+", this::addMessageClick);
        addMessage.DefaultBackgroundColor = Theme.GREEN_TRANSPARENT;
        addMessage.HoveredBackgroundColor = Theme.GREEN;
        addMessage.HoveredForegroundColor = Theme.WHITE;
        addMessage.OutlineColor = 0x00000000;
        addMessage.ShowOutline = false;
        addMessage.CornerRadius = 7;
        addMessage.CircleMode = true;
        addCommand = new CustomButton(0, 0, 14, 14, "+", this::addCommandClick);
        addCommand.DefaultBackgroundColor = Theme.GREEN_TRANSPARENT;
        addCommand.HoveredBackgroundColor = Theme.GREEN;
        addCommand.HoveredForegroundColor = Theme.WHITE;
        addCommand.OutlineColor = 0x00000000;
        addCommand.ShowOutline = false;
        addCommand.CornerRadius = 7;
        addCommand.CircleMode = true;

        this.trigger = trigger;
        this.fontRenderer = mc.font;
        this.mc = mc;

        textBoxName = new CustomTextBox(fontRenderer, 0, 0, 200, 20, "");
        textBoxName.tag = "Name";
        textBoxName.setText(trigger.name != null ? trigger.name : "");
        checkBoxActive = new CheckBox(0, 0, 80, true, "Active", trigger.isActive, this::checkBoxClick);
        checkBoxActive.CornerRadius = 7;

        deleteEntry = new CustomButton(0, 0, 14, 14, "x", this::deleteEntryClick);
        deleteEntry.DefaultBackgroundColor = Theme.RED_TRANSPARENT;
        deleteEntry.HoveredBackgroundColor = Theme.RED;
        deleteEntry.HoveredForegroundColor = Theme.WHITE;
        deleteEntry.OutlineColor = Theme.RED;
        deleteEntry.ShowOutline = false;
        deleteEntry.CornerRadius = 7;
        deleteEntry.CircleMode = true;

        if (kind == TriggerKind.Donate) {
            textBoxFrom = new CustomTextBox(fontRenderer, 0, 0, 80, 20, "0");
            textBoxFrom.tag = "From";
            textBoxTo = new CustomTextBox(fontRenderer, 0, 0, 80, 20, "999999");
            textBoxTo.tag = "To";
            textBoxCurrency = new CustomTextBox(fontRenderer, 0, 0, 80, 20, "");
            textBoxCurrency.tag = "Currency";
        } else if (kind == TriggerKind.Subscribe) {
            subscribeType = "YouTubeSubscription";
            cycleLeft = new CustomButton(0, 0, 14, 14, "<", this::cycleSubscribeLeft);
            cycleRight = new CustomButton(0, 0, 14, 14, ">", this::cycleSubscribeRight);
            cycleLeft.ShowOutline = false;
            cycleRight.ShowOutline = false;
            cycleLeft.CornerRadius = 7;
            cycleRight.CornerRadius = 7;
            cycleLeft.CircleMode = true;
            cycleRight.CircleMode = true;
        } else if (kind == TriggerKind.Twitch) {
            textBoxFrom = new CustomTextBox(fontRenderer, 0, 0, 80, 20, "0");
            textBoxFrom.tag = "From";
            textBoxTo = new CustomTextBox(fontRenderer, 0, 0, 80, 20, "999999");
            textBoxTo.tag = "To";
            twitchSubType = "Points";
            cycleLeft = new CustomButton(0, 0, 14, 14, "<", this::cycleTwitchLeft);
            cycleRight = new CustomButton(0, 0, 14, 14, ">", this::cycleTwitchRight);
            cycleLeft.ShowOutline = false;
            cycleRight.ShowOutline = false;
            cycleLeft.CornerRadius = 7;
            cycleRight.CornerRadius = 7;
            cycleLeft.CircleMode = true;
            cycleRight.CircleMode = true;
        }

        loadFromTrigger(trigger);
    }

    public void setRight(int right) {
        this.right = right;
    }

    public void setShowActiveToggle(boolean showActiveToggle) {
        this.showActiveToggle = showActiveToggle;
    }

    public static TriggerKind detectKind(TriggerDto trigger) {
        if (trigger.sensitives != null) {
            for (var sensitive : trigger.sensitives) {
                if (sensitive.properties == null || sensitive.properties.type == null)
                    continue;
                var type = sensitive.properties.type;
                var shortType = type.contains("/") ? type.substring(type.lastIndexOf("/") + 1) : type;
                if ("donate".equals(shortType))
                    return TriggerKind.Donate;
                if ("subscribe".equals(shortType))
                    return TriggerKind.Subscribe;
                if ("points".equals(shortType) || "bits".equals(shortType))
                    return TriggerKind.Twitch;
            }
        }
        return TriggerKind.Donate;
    }

    private void loadFromTrigger(TriggerDto trigger) {
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
                } else if ("subscribe".equals(shortType) && sensitive.properties.value instanceof SubscribeSensitiveProperties) {
                    var sp = (SubscribeSensitiveProperties) sensitive.properties.value;
                    if (sp.type != null)
                        subscribeType = sp.type;
                } else if (sensitive.properties.value instanceof TwitchPointsSensitiveProperties) {
                    var tp = (TwitchPointsSensitiveProperties) sensitive.properties.value;
                    textBoxFrom.setText(String.valueOf(tp.from));
                    textBoxTo.setText(String.valueOf(tp.to));
                    twitchSubType = "Points";
                } else if (sensitive.properties.value instanceof TwitchBitsSensitiveProperties) {
                    var tb = (TwitchBitsSensitiveProperties) sensitive.properties.value;
                    textBoxFrom.setText(String.valueOf(tb.from));
                    textBoxTo.setText(String.valueOf(tb.to));
                    twitchSubType = "Bits";
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
                    var line = new WritableLineElement(this, mc, WritableType.Message, "Msg " + (messages.size() + 1));
                    if (handler.properties.value instanceof MessageHandlerProperties)
                        line.line.setText(((MessageHandlerProperties) handler.properties.value).message);
                    else if (handler.properties.value instanceof String)
                        line.line.setText((String) handler.properties.value);
                    messages.add(line);
                } else if ("command".equals(shortType)) {
                    var line = new WritableLineElement(this, mc, WritableType.Command, "Cmd " + (commands.size() + 1));
                    if (handler.properties.value instanceof CommandHandlerProperties)
                        line.line.setText(((CommandHandlerProperties) handler.properties.value).command);
                    else if (handler.properties.value instanceof String)
                        line.line.setText((String) handler.properties.value);
                    commands.add(line);
                }
            }
        }
    }

    private void cycleSubscribeLeft() {
        int idx = indexOf(SUBSCRIBE_TYPES, subscribeType);
        idx = (idx - 1 + SUBSCRIBE_TYPES.length) % SUBSCRIBE_TYPES.length;
        subscribeType = SUBSCRIBE_TYPES[idx];
    }

    private void cycleSubscribeRight() {
        int idx = indexOf(SUBSCRIBE_TYPES, subscribeType);
        idx = (idx + 1) % SUBSCRIBE_TYPES.length;
        subscribeType = SUBSCRIBE_TYPES[idx];
    }

    private void cycleTwitchLeft() {
        int idx = indexOf(TWITCH_SUB_TYPES, twitchSubType);
        idx = (idx - 1 + TWITCH_SUB_TYPES.length) % TWITCH_SUB_TYPES.length;
        twitchSubType = TWITCH_SUB_TYPES[idx];
    }

    private void cycleTwitchRight() {
        int idx = indexOf(TWITCH_SUB_TYPES, twitchSubType);
        idx = (idx + 1) % TWITCH_SUB_TYPES.length;
        twitchSubType = TWITCH_SUB_TYPES[idx];
    }

    private static int indexOf(String[] arr, String value) {
        for (int i = 0; i < arr.length; i++)
            if (arr[i].equals(value)) return i;
        return 0;
    }

    private void addMessageClick() {
        messages.add(new WritableLineElement(this, mc, WritableType.Message, "Msg " + (messages.size() + 1)));
        owner.updateHeight();
    }

    private void addCommandClick() {
        commands.add(new WritableLineElement(this, mc, WritableType.Command, "Cmd " + (commands.size() + 1)));
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
        if (showActiveToggle) {
            checkBoxActive.mouseClicked(event, doubleClick);
        }
        addMessage.mouseClicked(event, doubleClick);
        addCommand.mouseClicked(event, doubleClick);
        deleteEntry.mouseClicked(event, doubleClick);
        if (kind == TriggerKind.Donate || kind == TriggerKind.Twitch) {
            textBoxFrom.mouseClicked(event, doubleClick);
            textBoxTo.mouseClicked(event, doubleClick);
        }
        if (kind == TriggerKind.Donate) {
            textBoxCurrency.mouseClicked(event, doubleClick);
        }
        if (kind == TriggerKind.Subscribe || kind == TriggerKind.Twitch) {
            cycleLeft.mouseClicked(event, doubleClick);
            cycleRight.mouseClicked(event, doubleClick);
        }
        for (var msg : List.copyOf(messages)) {
            msg.line.mouseClicked(event, doubleClick);
            msg.delete.mouseClicked(event, doubleClick);
        }
        for (var cmd : List.copyOf(commands)) {
            cmd.line.mouseClicked(event, doubleClick);
            cmd.delete.mouseClicked(event, doubleClick);
        }
        return true;
    }

    @Override
    public void charTyped(CharacterEvent event) {
        textBoxName.charTyped(event);
        if (kind == TriggerKind.Donate || kind == TriggerKind.Twitch) {
            textBoxFrom.charTyped(event);
            textBoxTo.charTyped(event);
        }
        if (kind == TriggerKind.Donate) {
            textBoxCurrency.charTyped(event);
        }
        for (var msg : messages)
            msg.line.charTyped(event);
        for (var cmd : commands)
            cmd.line.charTyped(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        textBoxName.keyPressed(event);
        if (kind == TriggerKind.Donate || kind == TriggerKind.Twitch) {
            textBoxFrom.keyPressed(event);
            textBoxTo.keyPressed(event);
        }
        if (kind == TriggerKind.Donate) {
            textBoxCurrency.keyPressed(event);
        }
        for (var msg : messages)
            msg.line.keyPressed(event);
        for (var cmd : commands)
            cmd.line.keyPressed(event);
        return true;
    }

    @Override
    public void drawEntry(GuiGraphicsExtractor graphics, int x, int y, int mouseX, int mouseY, float partialTicks) {
        int cardW = right - x - 12;
        int entryH = getHeightE();
        boolean hovered = mouseX >= x && mouseX <= x + cardW && mouseY >= y && mouseY <= y + entryH;
        Theme.fillCard(graphics, x, y, cardW, entryH - 6, hovered);

        int accentColor = switch (kind) {
            case Donate -> Theme.YELLOW;
            case Subscribe -> Theme.ACCENT;
            case Twitch -> Theme.GREEN;
        };
        Theme.drawLeftAccentBar(graphics, x, y, entryH - 6, accentColor);

        int offset = y + 6;
        int innerX = x + 12;
        int rowH = 22;
        int fieldGap = 8;

        // Row 0: Name + Delete
        textBoxName.renderButton(graphics, innerX, offset, partialTicks);
        if (showActiveToggle) {
            checkBoxActive.drawButton(graphics, mc, innerX + 208, offset + 1, mouseX, mouseY, partialTicks);
        }
        deleteEntry.drawButton(graphics, mc, x + cardW - 20, centerY(offset, 20, 14), mouseX, mouseY, partialTicks);
        offset += rowH + 14;

        if (kind == TriggerKind.Donate) {
            textBoxFrom.renderButton(graphics, innerX, offset, partialTicks);
            textBoxTo.renderButton(graphics, innerX + 88, offset, partialTicks);
            textBoxCurrency.renderButton(graphics, innerX + 176, offset, partialTicks);
            offset += rowH + 14;
        } else if (kind == TriggerKind.Subscribe) {
            graphics.text(fontRenderer, FontHelper.comp("TYPE"), innerX, offset, Theme.TEXT_MUTED, false);
            int typeBtnOff = innerX + FontHelper.width(fontRenderer, "TYPE") + fieldGap;
            cycleLeft.drawButton(graphics, mc, typeBtnOff, centerY(offset, 10, 14), mouseX, mouseY, partialTicks);
            int typeW = FontHelper.width(fontRenderer, subscribeType);
            graphics.text(fontRenderer, FontHelper.comp(subscribeType), typeBtnOff + 20, offset + 2, Theme.TEXT_ACCENT, false);
            cycleRight.drawButton(graphics, mc, typeBtnOff + 22 + typeW + 2, centerY(offset, 10, 14), mouseX, mouseY, partialTicks);
            offset += rowH + 14;
        } else if (kind == TriggerKind.Twitch) {
            graphics.text(fontRenderer, FontHelper.comp("TYPE"), innerX, offset, Theme.TEXT_MUTED, false);
            int typeBtnOff = innerX + FontHelper.width(fontRenderer, "TYPE") + fieldGap;
            cycleLeft.drawButton(graphics, mc, typeBtnOff, centerY(offset, 10, 14), mouseX, mouseY, partialTicks);
            int typeW = FontHelper.width(fontRenderer, twitchSubType);
            graphics.text(fontRenderer, FontHelper.comp(twitchSubType), typeBtnOff + 20, offset + 2, Theme.TEXT_ACCENT, false);
            cycleRight.drawButton(graphics, mc, typeBtnOff + 22 + typeW + 2, centerY(offset, 10, 14), mouseX, mouseY, partialTicks);
            offset += rowH + 2;
            textBoxFrom.renderButton(graphics, innerX, offset, partialTicks);
            textBoxTo.renderButton(graphics, innerX + 88, offset, partialTicks);
            offset += rowH + 14;
        }

        // Messages section (header always visible)
        int msgLabelW = FontHelper.width(fontRenderer, "MESSAGES");
        graphics.text(fontRenderer, FontHelper.comp("MESSAGES"), innerX, offset, Theme.TEXT_MUTED, false);
        addMessage.drawButton(graphics, mc, innerX + msgLabelW + 8, centerY(offset, 8, 14), mouseX, mouseY, partialTicks);
        offset += 12;

        for (int i = 0; i < messages.size(); i++) {
            messages.get(i).drawElement(graphics, innerX, offset, mouseX, mouseY, partialTicks);
            offset += 30;
        }

        offset += 6;

        // Commands section (header always visible)
        int cmdLabelW = FontHelper.width(fontRenderer, "COMMANDS");
        graphics.text(fontRenderer, FontHelper.comp("COMMANDS"), innerX, offset, Theme.TEXT_MUTED, false);
        addCommand.drawButton(graphics, mc, innerX + cmdLabelW + 8, centerY(offset, 8, 14), mouseX, mouseY, partialTicks);
        offset += 12;

        for (int i = 0; i < commands.size(); i++) {
            commands.get(i).drawElement(graphics, innerX, offset, mouseX, mouseY, partialTicks);
            offset += 30;
        }

        offset = renderPreview(graphics, innerX, offset, cardW - 24);

        offset += 6;
    }

    private int centerY(int rowY, int rowH, int elementH) {
        return rowY + (rowH - elementH) / 2;
    }

    private int renderPreview(GuiGraphicsExtractor graphics, int x, int y, int width) {
        boolean hasMessage = !messages.isEmpty() && !messages.get(0).line.getText().isEmpty();
        boolean hasCommand = !commands.isEmpty() && !commands.get(0).line.getText().isEmpty();
        if (!hasMessage && !hasCommand) return y;

        graphics.text(fontRenderer, FontHelper.comp("PREVIEW"), x, y, Theme.TEXT_MUTED, false);
        y += 12;

        if (hasMessage) {
            Theme.fillPanel(graphics, x, y, width, 18);
            graphics.text(fontRenderer, FontHelper.comp("> " + getMessagePreview()), x + 5, y + 5, Theme.TEXT_SECONDARY, false);
            y += 24;
        }

        if (hasCommand) {
            Theme.fillPanel(graphics, x, y, width, 18);
            graphics.text(fontRenderer, FontHelper.comp("/" + getCommandPreview()), x + 5, y + 5, Theme.TEXT_SECONDARY, false);
            y += 24;
        }

        return y;
    }

    private String getMessagePreview() {
        return messages.get(0).line.getText()
                .replace("{username}", "Player")
                .replace("{amount}", "100")
                .replace("{currency}", "USD")
                .replace("{playername}", "Steve")
                .replace("{message}", "Hello!");
    }

    private String getCommandPreview() {
        return commands.get(0).line.getText()
                .replace("{username}", "Player")
                .replace("{amount}", "100")
                .replace("{currency}", "USD")
                .replace("{playername}", "Steve")
                .replace("{message}", "Hello!");
    }

    @Override
    public int getHeightE() {
        int base = 36; // name row
        if (kind == TriggerKind.Subscribe) {
            base += 36; // type cycle
        } else if (kind == TriggerKind.Twitch) {
            base += 60; // subtype cycle + from/to
        } else {
            base += 36; // from/to/currency
        }
        base += 12; // messages header
        base += messages.size() * 30;
        base += 18; // gap + commands header
        base += commands.size() * 30;
        int previewLines = 0;
        if (!messages.isEmpty() && !messages.get(0).line.getText().isEmpty()) previewLines++;
        if (!commands.isEmpty() && !commands.get(0).line.getText().isEmpty()) previewLines++;
        if (previewLines > 0)
            base += 12 + previewLines * 24;
        base += 18; // padding
        return base;
    }

    public void removeMessage(WritableLineElement ctb) {
        messages.remove(ctb);
        for (int i = 0; i < messages.size(); i++)
            messages.get(i).changeTag("Msg " + (i + 1));
    }

    public void removeCommands(WritableLineElement ctb) {
        commands.remove(ctb);
        for (int i = 0; i < commands.size(); i++)
            commands.get(i).changeTag("Cmd " + (i + 1));
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
        return textBoxFrom != null ? textBoxFrom.getText() : "0";
    }

    public String getTo() {
        return textBoxTo != null ? textBoxTo.getText() : "999999";
    }

    public String getCurrency() {
        return textBoxCurrency != null ? textBoxCurrency.getText() : "";
    }

    public TriggerKind getKind() {
        return kind;
    }

    public String getSubscribeType() {
        return subscribeType;
    }

    public String getTwitchSubType() {
        return twitchSubType;
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
            line = new CustomTextBox(mc.font, 0, 0, 210, 20, "");
            line.tag = tag;
            delete = new CustomButton(0, 0, 14, 14, "x", () -> deleteClick());
            delete.DefaultBackgroundColor = Theme.RED_TRANSPARENT;
            delete.HoveredBackgroundColor = Theme.RED;
            delete.HoveredForegroundColor = Theme.WHITE;
            delete.OutlineColor = Theme.RED;
            delete.ShowOutline = false;
            delete.CornerRadius = 7;
            delete.CircleMode = true;
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
            delete.drawButton(graphics, mc, x + 218, centerY(y, 20, 14), mouseX, mouseY, partialTicks);
        }
    }
}
