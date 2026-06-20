package net.bummy1337.daintegrate.fabric.screen;

import net.bummy1337.daintegrate.Constants;
import net.bummy1337.daintegrate.configurations.HandlerPropertiesDto;
import net.bummy1337.daintegrate.configurations.PropertiesDto;
import net.bummy1337.daintegrate.configurations.SensitivePropertiesDto;
import net.bummy1337.daintegrate.configurations.SettingsDto;
import net.bummy1337.daintegrate.configurations.TriggerDto;
import net.bummy1337.daintegrate.configurations.sources.FileConfigurationSource;
import net.bummy1337.daintegrate.fabric.gui.*;
import net.bummy1337.daintegrate.handlers.CommandHandlerProperties;
import net.bummy1337.daintegrate.handlers.MessageHandlerProperties;
import net.bummy1337.daintegrate.sensitives.DonateSensitiveProperties;
import net.bummy1337.dontaionalerts.DonationAlertsClient;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

import java.awt.Desktop;
import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainScreen extends Screen {
    enum PanelsType { Messages, Status, Types, Settings, Help }

    private static final int SPACE_LEFTBUTTON = 20;
    private static final int TOKENPANELY = 60;

    private static List<String> langHelpLines;

    private Minecraft mc;
    private Font fontRenderer;
    private FileConfigurationSource configurationSource;
    private DonationAlertsClient client;
    private PanelsType activePanel;
    private SettingsDto currentSettings;

    private List<ReadOnlyDonationAlertsEvent> donations;

    private CustomButton LEFTBUTTON_Messages;
    private CustomButton LEFTBUTTON_Status;
    private CustomButton LEFTBUTTON_Types;
    private CustomButton LEFTBUTTON_Settings;
    private CustomButton LEFTBUTTON_Help;
    private CustomButton LEFTBUTTON_SupportAuthor;

    private DefaultButton STATUSBUTTON_Save;
    private DefaultButton STATUSBUTTON_Delete;
    private DefaultButton STATUSBUTTON_ConnectionController;

    private DefaultButton TYPESBUTTON_Save;
    private CustomButton TYPESBUTTON_Add;

    private CheckBox SETTINGSBUTTON_SkippingTestDonation;
    private DefaultButton SETTINGSBUTTON_Save;

    private CustomTextBox text1;

    private ScrollPanel<MessageEntry> messagesPanel;
    private ScrollPanel<DonationTypeEntry> typesPanel;

    private boolean typesSuccessfulSave;
    private int typesSaveTimer;
    private boolean initialized;

    public MainScreen(FileConfigurationSource configurationSource, DonationAlertsClient client, List<ReadOnlyDonationAlertsEvent> donations) {
        super(Component.literal("MainWindow"));
        this.mc = Minecraft.getInstance();
        fontRenderer = mc.font;
        this.configurationSource = configurationSource;
        this.client = client;
        this.donations = donations;
        this.currentSettings = configurationSource.getCurrent();
        if (this.currentSettings == null)
            this.currentSettings = new SettingsDto();
    }

    @Override
    protected void init() {
        initialized = false;
        LEFTBUTTON_Messages = addRenderableWidget(new CustomButton(0, 20, 120, 20, "Messages", this::switchPanel_Messages));
        LEFTBUTTON_Status = addRenderableWidget(new CustomButton(0, 20 + SPACE_LEFTBUTTON, 120, 20, "Status", this::switchPanel_Status));
        LEFTBUTTON_Types = addRenderableWidget(new CustomButton(0, 20 + (2 * SPACE_LEFTBUTTON), 120, 20, "Types", this::switchPanel_Types));
        LEFTBUTTON_Settings = addRenderableWidget(new CustomButton(0, 20 + (3 * SPACE_LEFTBUTTON), 120, 20, "Settings", this::switchPanel_Settings));
        LEFTBUTTON_Help = addRenderableWidget(new CustomButton(0, 20 + (4 * SPACE_LEFTBUTTON), 120, 20, "Help", this::switchPanel_Help));
        LEFTBUTTON_SupportAuthor = addRenderableWidget(new DefaultButton(0, height - 20, 120, 20, "Support Author", this::switchPanel_SupportAuthor));
        SETTINGSBUTTON_SkippingTestDonation = addRenderableWidget(new CheckBox(125, 25, 170, activePanel == PanelsType.Settings, "Skip Test Donation", currentSettings != null && currentSettings.skipTestDonation, this::settingsSkipTestDonationClick));
        SETTINGSBUTTON_Save = addRenderableWidget(new DefaultButton(this.width - 80, 0, 80, activePanel == PanelsType.Settings, "Save", this::settingsSaveClick));
        STATUSBUTTON_Save = addRenderableWidget(new DefaultButton(125, TOKENPANELY + 45, 120, activePanel == PanelsType.Status, "Save", this::statusSaveClick));
        STATUSBUTTON_Delete = addRenderableWidget(new DefaultButton(250, TOKENPANELY + 45, 120, activePanel == PanelsType.Status, "Delete", this::statusDeleteClick));
        STATUSBUTTON_ConnectionController = addRenderableWidget(new DefaultButton(125, 35, 120, activePanel == PanelsType.Status,
                client.getConnected() ? "Disconnect" : "Connect", this::connectionControllerClick));
        TYPESBUTTON_Save = addRenderableWidget(new DefaultButton(this.width - 80, 0, 80, activePanel == PanelsType.Types, "Save", this::typesSaveClick));
        TYPESBUTTON_Add = addRenderableWidget(new CustomButton(this.width - 100, 0, 20, activePanel == PanelsType.Types, "+", this::typesAddClick));
        TYPESBUTTON_Add.DefaultBackgroundColor = Palette.GREEN;
        TYPESBUTTON_Add.HoveredBackgroundColor = Palette.GREEN_HOVERED;
        TYPESBUTTON_Add.HoveredForegroundColor = Palette.WHITE;
        LEFTBUTTON_SupportAuthor.DefaultBackgroundColor = Palette.BLACK_TRANSPARENT30;

        text1 = new CustomTextBox(fontRenderer, 125, TOKENPANELY + 10, 200, 20, "");
        text1.setTextColor(Palette.WHITE);
        text1.tag = "Token:";

        initializeMessages();
        initializeTypes();

        if (activePanel == null)
            this.SetActivePanel(PanelsType.Messages);
        initialized = true;
    }

    private void initializeMessages() {
        messagesPanel = new ScrollPanel<MessageEntry>(125, 25, this.width, this.height);
        for (int i = donations.size() - 1; i >= 0; i--) {
            var d = donations.get(i);
            messagesPanel.addEntry(new MessageEntry(fontRenderer, d.getUserName(), String.valueOf(d.getAmount()), d.getCurrency(), d.getMessage(), this.width - 130));
        }
    }

    private void initializeTypes() {
        typesPanel = new ScrollPanel<DonationTypeEntry>(125, 25, this.width, this.height);
        if (currentSettings != null && currentSettings.triggers != null) {
            for (int i = 0; i < currentSettings.triggers.length; i++)
                typesPanel.addEntry(new DonationTypeEntry(typesPanel, currentSettings.triggers[i], mc, this.width));
        }
    }

    private void initializeLangHelpLines() {
        langHelpLines = new ArrayList<>();
        langHelpLines.add("=== Help ===");
        langHelpLines.add("{message} - Donation message");
        langHelpLines.add("{amount} - Donation amount");
        langHelpLines.add("{currency} - Donation currency");
        langHelpLines.add("{username} - Donator username");
        langHelpLines.add("{playername} - Minecraft player name");
    }

    @Override
    public void tick() {
        if (typesSaveTimer > 0)
            typesSaveTimer--;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (!initialized)
            return;

        graphics.fill(0, 0, this.width, this.height, 0xEE1A1A1E);

        if (activePanel == null)
            activePanel = PanelsType.Messages;

        switch (activePanel) {
            case Messages:
                messagesPanel.drawPanel(graphics, mouseX, mouseY, delta);
                break;
            case Status:
                boolean connected = client.getConnected();
                graphics.text(fontRenderer, "Connection status:", 125, 25, Palette.WHITE, false);
                graphics.text(fontRenderer, connected ? "Connected." : "Disconnected.", 125 + fontRenderer.width("Connection status:") + 5, 25,
                        connected ? Palette.GREEN : Palette.RED, false);

                text1.renderButton(graphics);
                boolean tokenExists = new java.io.File(System.getProperty("user.home"), Constants.TokenFileName).exists();
                graphics.text(fontRenderer, tokenExists ? "Token exists." : "Token absent.", 125, TOKENPANELY + 35,
                        tokenExists ? Palette.GREEN : Palette.RED, false);
                break;
            case Types:
                typesPanel.drawPanel(graphics, mouseX, mouseY, delta);
                if (typesSuccessfulSave && typesSaveTimer > 0)
                    graphics.text(fontRenderer, "Saved", width - fontRenderer.width("Saved") - 100, 6, Palette.WHITE, false);
                else if (!typesSuccessfulSave && typesSaveTimer > 0)
                    graphics.text(fontRenderer, "Error", width - fontRenderer.width("Error") - 100, 6, Palette.RED, false);
                break;
            case Settings:
                SETTINGSBUTTON_SkippingTestDonation.drawButton(graphics, mouseX, mouseY, delta);
                graphics.text(fontRenderer, "Donation Alerts Integrate", 125, 5, Palette.WHITE, false);
                break;
            case Help:
                if (langHelpLines == null) initializeLangHelpLines();
                for (int i = 0; i < langHelpLines.size(); i++)
                    graphics.text(fontRenderer, langHelpLines.get(i), 125, i * 10 + 25, Palette.WHITE, false);
                break;
        }

        graphics.fill(0, 0, this.width, 20, 0x65000000);
        graphics.fill(0, 20, 120, this.height, 0x50000000);
        graphics.text(fontRenderer, "Donation Alerts Integrate", 5, 5, Palette.WHITE, false);

        super.extractRenderState(graphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (!initialized)
            return super.charTyped(event);
        if (activePanel == PanelsType.Status)
            text1.charTyped(event);
        else if (activePanel == PanelsType.Types)
            typesPanel.charTyped(event);
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!initialized)
            return super.keyPressed(event);
        if (activePanel == PanelsType.Status)
            text1.keyPressed(event);
        else if (activePanel == PanelsType.Types)
            typesPanel.keyPressed(event);
        if (event.key() == 256)
            return super.keyPressed(event);
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!initialized)
            return super.mouseClicked(event, doubleClick);
        super.mouseClicked(event, doubleClick);
        if (activePanel == PanelsType.Status)
            text1.mouseClicked(event, doubleClick);
        if (activePanel == PanelsType.Types)
            typesPanel.mouseClicked(event, doubleClick);
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (activePanel == PanelsType.Messages)
            messagesPanel.mouseScrolled(mouseX, mouseY, deltaY);
        if (activePanel == PanelsType.Types)
            typesPanel.mouseScrolled(mouseX, mouseY, deltaY);
        return true;
    }

    private void switchPanel_Messages() {
        SetActivePanel(PanelsType.Messages);
    }

    private void switchPanel_Status() {
        SetActivePanel(PanelsType.Status);
    }

    private void switchPanel_Types() {
        SetActivePanel(PanelsType.Types);
    }

    private void switchPanel_Settings() {
        SetActivePanel(PanelsType.Settings);
    }

    private void switchPanel_Help() {
        SetActivePanel(PanelsType.Help);
    }

    private void switchPanel_SupportAuthor() {
        try {
            Desktop.getDesktop().browse(new URI("https://t.me/bummy1337"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void settingsSkipTestDonationClick() {
        SETTINGSBUTTON_SkippingTestDonation.SwitchFlag(!SETTINGSBUTTON_SkippingTestDonation.Flag);
        currentSettings.skipTestDonation = SETTINGSBUTTON_SkippingTestDonation.Flag;
        settingsSaveClick();
    }

    private void settingsSaveClick() {
        try {
            if (configurationSource != null && currentSettings != null) {
                configurationSource.save(currentSettings);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void statusSaveClick() {
        try {
            var home = System.getProperty("user.home");
            var writer = new PrintWriter(new java.io.File(home, Constants.TokenFileName), StandardCharsets.UTF_8);
            writer.println(text1.getText());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void statusDeleteClick() {
        try {
            var home = System.getProperty("user.home");
            var file = new java.io.File(home, Constants.TokenFileName);
            if (file.exists()) file.delete();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void connectionControllerClick() {
        if (client.getConnected()) {
            client.disconnect();
            STATUSBUTTON_ConnectionController.setMessage(Component.literal("Connect"));
        } else {
            var home = System.getProperty("user.home");
            var token = "";
            try (BufferedReader br = new BufferedReader(new FileReader(new java.io.File(home, Constants.TokenFileName)))) {
                String line;
                while ((line = br.readLine()) != null && line.length() > 3) {
                    token = line.trim();
                }
            } catch (FileNotFoundException e) {
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
            if (!token.isEmpty()) {
                client.connect(token);
            }
            STATUSBUTTON_ConnectionController.setMessage(Component.literal("Disconnect"));
        }
    }

    private void typesSaveClick() {
        List<DonationTypeEntry> entries = typesPanel.getEntries();
        try {
            List<TriggerDto> newTriggers = new ArrayList<>();
            for (int i = 0; i < entries.size(); i++) {
                if (entries.get(i).hasError) {
                    typesSaveTimer = 120;
                    typesSuccessfulSave = false;
                    return;
                }
                var entry = entries.get(i);
                var trigger = new TriggerDto();
                trigger.name = entry.getName();
                trigger.isActive = entry.getActive();

                var handlerList = new ArrayList<HandlerPropertiesDto>();
                for (var msg : entry.getMessages()) {
                    var hp = new HandlerPropertiesDto();
                    var msgProps = new MessageHandlerProperties();
                    msgProps.message = msg;
                    hp.properties = new PropertiesDto<MessageHandlerProperties>();
                    hp.properties.type = Constants.ModId + "/handler/message";
                    hp.properties.value = msgProps;
                    hp.delay = 0;
                    handlerList.add(hp);
                }
                for (var cmd : entry.getCommands()) {
                    var hp = new HandlerPropertiesDto();
                    var cmdProps = new CommandHandlerProperties();
                    cmdProps.command = cmd;
                    hp.properties = new PropertiesDto<CommandHandlerProperties>();
                    hp.properties.type = Constants.ModId + "/handler/command";
                    hp.properties.value = cmdProps;
                    hp.delay = 0;
                    handlerList.add(hp);
                }
                trigger.handlers = handlerList.toArray(new HandlerPropertiesDto[0]);

                var sensitive = new SensitivePropertiesDto();
                sensitive.properties = new PropertiesDto();
                sensitive.properties.type = Constants.ModId + "/sensitive/donate";
                var donateProps = new DonateSensitiveProperties();
                try {
                    donateProps.from = Float.parseFloat(entry.getFrom());
                } catch (NumberFormatException e) {
                    donateProps.from = 0;
                }
                try {
                    donateProps.to = Float.parseFloat(entry.getTo());
                } catch (NumberFormatException e) {
                    donateProps.to = 999999;
                }
                donateProps.currency = entry.getCurrency();
                sensitive.properties.value = donateProps;
                trigger.sensitives = new SensitivePropertiesDto[]{sensitive};

                newTriggers.add(trigger);
            }
            currentSettings.triggers = newTriggers.toArray(new TriggerDto[0]);
            configurationSource.save(currentSettings);
            typesSaveTimer = 120;
            typesSuccessfulSave = true;
        } catch (Exception e) {
            e.printStackTrace();
            typesSaveTimer = 120;
            typesSuccessfulSave = false;
        }
    }

    private void typesAddClick() {
        var newTrigger = new TriggerDto();
        newTrigger.name = "New Trigger";
        newTrigger.isActive = true;
        var donateProps = new DonateSensitiveProperties();
        donateProps.from = 0;
        donateProps.to = 999999;
        donateProps.currency = "";
        var sp = new SensitivePropertiesDto();
        sp.properties = new PropertiesDto();
        sp.properties.type = Constants.ModId + "/sensitive/donate";
        sp.properties.value = donateProps;
        newTrigger.sensitives = new SensitivePropertiesDto[]{sp};
        typesPanel.addEntry(new DonationTypeEntry(typesPanel, newTrigger, mc, this.width));
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void SetActivePanel(PanelsType t) {
        if (activePanel == t)
            return;

        if (activePanel == PanelsType.Status)
            text1.setFocus(false);
        setVisibilitySettingsPanel(false);
        setVisibilityStatusPanel(false);
        setVisibilityMessagesPanel(false);
        setVisibilityTypesPanel(false);
        switch (t) {
            case Messages:
                setVisibilityMessagesPanel(true);
                break;
            case Status:
                setVisibilityStatusPanel(true);
                break;
            case Types:
                setVisibilityTypesPanel(true);
                break;
            case Settings:
                setVisibilitySettingsPanel(true);
                break;
            case Help:
                if (langHelpLines == null) initializeLangHelpLines();
                break;
        }
        activePanel = t;
    }

    void setVisibilitySettingsPanel(boolean value) {
        SETTINGSBUTTON_SkippingTestDonation.visible =
                SETTINGSBUTTON_Save.visible = value;
    }

    void setVisibilityStatusPanel(boolean value) {
        STATUSBUTTON_Save.visible =
                STATUSBUTTON_Delete.visible =
                        STATUSBUTTON_ConnectionController.visible = value;
    }

    void setVisibilityMessagesPanel(boolean value) {
        messagesPanel.visible = value;
    }

    void setVisibilityTypesPanel(boolean value) {
        TYPESBUTTON_Save.visible =
                TYPESBUTTON_Add.visible = value;
    }
}
