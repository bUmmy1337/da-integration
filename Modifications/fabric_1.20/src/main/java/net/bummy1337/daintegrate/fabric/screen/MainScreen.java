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
import net.bummy1337.daintegrate.sensitives.SubscribeSensitiveProperties;
import net.bummy1337.daintegrate.sensitives.TwitchBitsSensitiveProperties;
import net.bummy1337.daintegrate.sensitives.TwitchPointsSensitiveProperties;
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
    enum Tab { Dashboard, Triggers, Connection, Settings }
    enum TriggerSubTab { Donate, Subscribe, Twitch }

    private Minecraft mc;
    private Font fontRenderer;
    private FileConfigurationSource configurationSource;
    private DonationAlertsClient client;
    private SettingsDto currentSettings;
    private List<ReadOnlyDonationAlertsEvent> donations;

    private int winX, winY, winW, winH;

    private Tab activeTab;
    private TriggerSubTab activeSubTab;

    private CustomButton tabDashboard, tabTriggers, tabConnection, tabSettings;
    private CustomButton subDonate, subSubscribe, subTwitch;
    private CustomButton btnClose;
    private CustomButton btnHelp;
    private CustomButton btnExport, btnImport;

    private DefaultButton btnSave;
    private CustomButton btnAdd;
    private CustomButton btnConnect;
    private CustomButton btnTokenSave, btnTokenDelete;

    private CheckBox chkSkipTest;
    private CustomTextBox txtToken;
    private CustomTextBox txtSearch;

    private ScrollPanel<MessageEntry> messagesPanel;
    private ScrollPanel<DonationTypeEntry> donatePanel;
    private ScrollPanel<DonationTypeEntry> subscribePanel;
    private ScrollPanel<DonationTypeEntry> twitchPanel;

    private boolean saveSuccess;
    private int saveTimer;
    private boolean initialized;

    private String pendingDeleteName;
    private boolean showConfirmDelete;

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
        FontHelper.setFontAvailable(true);
    }

    @Override
    protected void init() {
        initialized = false;

        winW = Math.min(this.width - 20, 640);
        winH = Math.min(this.height - 20, 460);
        winX = (this.width - winW) / 2;
        winY = (this.height - winH) / 2;

        int contentX = winX + Theme.PADDING;
        int contentW = winW - Theme.PADDING * 2;
        int titleY = winY + 4;
        int tabY = winY + Theme.TITLE_BAR_H;
        int contentY = tabY + Theme.TAB_BAR_H;
        int contentBottom = winY + winH - Theme.STATUS_BAR_H;
        int contentH = contentBottom - contentY;

        int closeSize = 18;
        btnClose = addWidget(new CustomButton(winX + winW - closeSize - 6, titleY + 3, closeSize, closeSize, "x", this::doClose));
        btnClose.DefaultBackgroundColor = 0x00000000;
        btnClose.HoveredBackgroundColor = Theme.RED;
        btnClose.HoveredForegroundColor = Theme.WHITE;
        btnClose.OutlineColor = 0x00000000;
        btnClose.OutlineHoverColor = 0x00000000;

        btnHelp = addWidget(new CustomButton(winX + winW - closeSize * 2 - 10, titleY + 3, closeSize, closeSize, "?", this::doHelp));
        btnHelp.DefaultBackgroundColor = 0x00000000;
        btnHelp.HoveredBackgroundColor = Theme.BG_ENTRY_HOVER;
        btnHelp.HoveredForegroundColor = Theme.TEXT_ACCENT;
        btnHelp.OutlineColor = 0x00000000;
        btnHelp.OutlineHoverColor = 0x00000000;

        btnExport = addWidget(new CustomButton(winX + winW - closeSize * 3 - 14, titleY + 3, closeSize, closeSize, "^", this::doExport));
        btnExport.DefaultBackgroundColor = 0x00000000;
        btnExport.HoveredBackgroundColor = Theme.BG_ENTRY_HOVER;
        btnExport.HoveredForegroundColor = Theme.GREEN;
        btnExport.OutlineColor = 0x00000000;
        btnExport.OutlineHoverColor = 0x00000000;

        btnImport = addWidget(new CustomButton(winX + winW - closeSize * 4 - 18, titleY + 3, closeSize, closeSize, "v", this::doImport));
        btnImport.DefaultBackgroundColor = 0x00000000;
        btnImport.HoveredBackgroundColor = Theme.BG_ENTRY_HOVER;
        btnImport.HoveredForegroundColor = Theme.ACCENT;
        btnImport.OutlineColor = 0x00000000;
        btnImport.OutlineHoverColor = 0x00000000;

        int tabW = 85;
        int tabSpacing = 4;
        int tabStartX = contentX;
        tabDashboard = addWidget(new CustomButton(tabStartX, tabY + 3, tabW, 20, "Dashboard", () -> switchTab(Tab.Dashboard)));
        tabTriggers = addWidget(new CustomButton(tabStartX + (tabW + tabSpacing), tabY + 3, tabW, 20, "Triggers", () -> switchTab(Tab.Triggers)));
        tabConnection = addWidget(new CustomButton(tabStartX + (tabW + tabSpacing) * 2, tabY + 3, tabW, 20, "Connection", () -> switchTab(Tab.Connection)));
        tabSettings = addWidget(new CustomButton(tabStartX + (tabW + tabSpacing) * 3, tabY + 3, tabW, 20, "Settings", () -> switchTab(Tab.Settings)));
        for (var tb : new CustomButton[]{tabDashboard, tabTriggers, tabConnection, tabSettings}) {
            tb.ShowOutline = false;
            tb.DefaultBackgroundColor = 0x00000000;
            tb.HoveredBackgroundColor = Theme.BG_PANEL_HOVER;
            tb.DefaultForegroundColor = Theme.TEXT_SECONDARY;
            tb.HoveredForegroundColor = Theme.TEXT_PRIMARY;
        }

        int subTabW = 70;
        int subTabY = contentY + 2;
        subDonate = addWidget(new CustomButton(contentX, subTabY, subTabW, 18, "Donate", () -> switchSubTab(TriggerSubTab.Donate)));
        subSubscribe = addWidget(new CustomButton(contentX + subTabW + 4, subTabY, subTabW + 10, 18, "Subscribe", () -> switchSubTab(TriggerSubTab.Subscribe)));
        subTwitch = addWidget(new CustomButton(contentX + (subTabW + 4) * 2 + 10, subTabY, subTabW, 18, "Twitch", () -> switchSubTab(TriggerSubTab.Twitch)));
        for (var sb : new CustomButton[]{subDonate, subSubscribe, subTwitch}) {
            sb.ShowOutline = false;
            sb.DefaultBackgroundColor = Theme.BG_ENTRY;
            sb.HoveredBackgroundColor = Theme.BG_ENTRY_HOVER;
            sb.DefaultForegroundColor = Theme.TEXT_SECONDARY;
            sb.HoveredForegroundColor = Theme.TEXT_PRIMARY;
        }

        txtSearch = new CustomTextBox(fontRenderer, contentX + winW - 220 - Theme.PADDING * 2, subTabY, 180, 18, "");
        txtSearch.tag = null;
        txtSearch.setSuggestion("Search...");

        int saveBtnW = 60;
        btnSave = addWidget(new DefaultButton(winX + winW - saveBtnW - Theme.PADDING, tabY + 3, saveBtnW, 20, "Save", this::typesSaveClick));
        btnAdd = addWidget(new CustomButton(winX + winW - saveBtnW - Theme.PADDING - 24, tabY + 3, 20, 20, "+", this::typesAddClick));
        btnAdd.DefaultBackgroundColor = Theme.GREEN;
        btnAdd.HoveredBackgroundColor = Theme.GREEN_TRANSPARENT;
        btnAdd.HoveredForegroundColor = Theme.WHITE;
        btnAdd.OutlineColor = Theme.GREEN;

        int panelY = contentY + 26;
        int panelH = contentBottom - panelY;

        messagesPanel = new ScrollPanel<>(contentX, panelY, contentX + contentW, panelY + panelH);
        donatePanel = new ScrollPanel<>(contentX, panelY, contentX + contentW, panelY + panelH);
        subscribePanel = new ScrollPanel<>(contentX, panelY, contentX + contentW, panelY + panelH);
        twitchPanel = new ScrollPanel<>(contentX, panelY, contentX + contentW, panelY + panelH);

        for (int i = donations.size() - 1; i >= 0; i--) {
            var d = donations.get(i);
            messagesPanel.addEntry(new MessageEntry(fontRenderer, d.getUserName(), String.valueOf(d.getAmount()), d.getCurrency(), d.getMessage(), contentW - 16));
        }

        if (currentSettings != null && currentSettings.triggers != null) {
            for (var trigger : currentSettings.triggers) {
                var kind = DonationTypeEntry.detectKind(trigger);
                switch (kind) {
                    case Subscribe -> subscribePanel.addEntry(new DonationTypeEntry(subscribePanel, trigger, mc, contentX + contentW, DonationTypeEntry.TriggerKind.Subscribe));
                    case Twitch -> twitchPanel.addEntry(new DonationTypeEntry(twitchPanel, trigger, mc, contentX + contentW, DonationTypeEntry.TriggerKind.Twitch));
                    default -> donatePanel.addEntry(new DonationTypeEntry(donatePanel, trigger, mc, contentX + contentW, DonationTypeEntry.TriggerKind.Donate));
                }
            }
        }

        txtToken = new CustomTextBox(fontRenderer, contentX, panelY + 40, 300, 20, "");
        txtToken.tag = "Token";
        txtToken.setTextColor(Theme.TEXT_PRIMARY);

        btnConnect = addWidget(new DefaultButton(contentX, panelY, 120, 20,
                client.getConnected() ? "Disconnect" : "Connect", this::connectionControllerClick));
        btnTokenSave = addWidget(new DefaultButton(contentX, panelY + 70, 100, 20, "Save Token", this::statusSaveClick));
        btnTokenDelete = addWidget(new DefaultButton(contentX + 110, panelY + 70, 100, 20, "Delete Token", this::statusDeleteClick));

        chkSkipTest = addWidget(new CheckBox(contentX, panelY, 200, true, "Skip Test Donation", currentSettings != null && currentSettings.skipTestDonation, this::settingsSkipTestDonationClick));

        if (activeTab == null)
            activeTab = Tab.Dashboard;
        if (activeSubTab == null)
            activeSubTab = TriggerSubTab.Donate;
        updateVisibility();
        initialized = true;
    }

    private void updateVisibility() {
        boolean isTriggers = activeTab == Tab.Triggers;
        boolean isConnection = activeTab == Tab.Connection;
        boolean isSettings = activeTab == Tab.Settings;

        for (var tb : new CustomButton[]{tabDashboard, tabTriggers, tabConnection, tabSettings}) {
            tb.DefaultForegroundColor = Theme.TEXT_SECONDARY;
        }
        switch (activeTab) {
            case Dashboard -> tabDashboard.DefaultForegroundColor = Theme.WHITE;
            case Triggers -> tabTriggers.DefaultForegroundColor = Theme.WHITE;
            case Connection -> tabConnection.DefaultForegroundColor = Theme.WHITE;
            case Settings -> tabSettings.DefaultForegroundColor = Theme.WHITE;
        }

        for (var sb : new CustomButton[]{subDonate, subSubscribe, subTwitch}) {
            sb.DefaultBackgroundColor = Theme.BG_ENTRY;
            sb.DefaultForegroundColor = Theme.TEXT_SECONDARY;
        }
        switch (activeSubTab) {
            case Donate -> { subDonate.DefaultBackgroundColor = Theme.ACCENT; subDonate.DefaultForegroundColor = Theme.WHITE; }
            case Subscribe -> { subSubscribe.DefaultBackgroundColor = Theme.ACCENT; subSubscribe.DefaultForegroundColor = Theme.WHITE; }
            case Twitch -> { subTwitch.DefaultBackgroundColor = Theme.ACCENT; subTwitch.DefaultForegroundColor = Theme.WHITE; }
        }

        messagesPanel.visible = activeTab == Tab.Dashboard;
        donatePanel.visible = isTriggers && activeSubTab == TriggerSubTab.Donate;
        subscribePanel.visible = isTriggers && activeSubTab == TriggerSubTab.Subscribe;
        twitchPanel.visible = isTriggers && activeSubTab == TriggerSubTab.Twitch;

        subDonate.visible = subSubscribe.visible = subTwitch.visible = isTriggers;
        txtSearch.setVisible(isTriggers);
        btnSave.visible = isTriggers;
        btnAdd.visible = isTriggers;

        btnConnect.visible = isConnection;
        txtToken.setVisible(isConnection);
        btnTokenSave.visible = isConnection;
        btnTokenDelete.visible = isConnection;

        chkSkipTest.visible = isSettings;
    }

    @Override
    public void tick() {
        if (saveTimer > 0) saveTimer--;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (!initialized) return;

        graphics.fill(0, 0, this.width, this.height, 0xB0000000);

        graphics.fill(winX, winY, winX + winW, winY + winH, Theme.BG_MAIN);
        graphics.outline(winX, winY, winW, winH, Theme.BORDER_FOCUS);

        graphics.fill(winX, winY, winX + winW, winY + Theme.TITLE_BAR_H, Theme.BG_PANEL);
        graphics.horizontalLine(winX, winX + winW, winY + Theme.TITLE_BAR_H, Theme.BORDER);

        graphics.text(fontRenderer, FontHelper.comp("DA INTEGRATE"),
                winX + Theme.PADDING + 2, winY + 8, Theme.TEXT_ACCENT, false);
        graphics.text(fontRenderer, FontHelper.comp("v" + getVersionSuffix()),
                winX + Theme.PADDING + 2 + FontHelper.width(fontRenderer, "DA INTEGRATE") + 6,
                winY + 8, Theme.TEXT_MUTED, false);

        graphics.fill(winX, winY + winH - Theme.STATUS_BAR_H, winX + winW, winY + winH, Theme.BG_PANEL);
        graphics.horizontalLine(winX, winX + winW, winY + winH - Theme.STATUS_BAR_H, Theme.BORDER);

        int statusY = winY + winH - Theme.STATUS_BAR_H + 7;
        int statusX = winX + Theme.PADDING;
        boolean connected = client.getConnected();
        Theme.drawStatusDot(graphics, statusX, statusY - 1, connected ? Theme.GREEN : Theme.RED);
        graphics.text(fontRenderer, FontHelper.comp(connected ? "Connected" : "Disconnected"),
                statusX + 12, statusY, connected ? Theme.GREEN : Theme.RED, false);

        int triggerCount = getTriggerCount();
        String countText = triggerCount + " trigger" + (triggerCount != 1 ? "s" : "");
        graphics.text(fontRenderer, FontHelper.comp(countText),
                statusX + 120, statusY, Theme.TEXT_MUTED, false);

        if (!donations.isEmpty()) {
            var last = donations.get(donations.size() - 1);
            String lastText = "Last: " + last.getUserName() + " - " + last.getAmount() + " " + last.getCurrency();
            int lastW = FontHelper.width(fontRenderer, lastText);
            graphics.text(fontRenderer, FontHelper.comp(lastText),
                    winX + winW - lastW - Theme.PADDING, statusY, Theme.TEXT_SECONDARY, false);
        }

        switch (activeTab) {
            case Dashboard -> {
                if (donations.isEmpty()) {
                    graphics.text(fontRenderer, FontHelper.comp("No events yet. Connect to start receiving!"),
                            winX + winW / 2 - FontHelper.width(fontRenderer, "No events yet. Connect to start receiving!") / 2,
                            winY + winH / 2, Theme.TEXT_MUTED, false);
                } else {
                    messagesPanel.drawPanel(graphics, mouseX, mouseY, delta);
                }
            }
            case Triggers -> {
                txtSearch.renderButton(graphics);
                switch (activeSubTab) {
                    case Donate -> donatePanel.drawPanel(graphics, mouseX, mouseY, delta);
                    case Subscribe -> subscribePanel.drawPanel(graphics, mouseX, mouseY, delta);
                    case Twitch -> twitchPanel.drawPanel(graphics, mouseX, mouseY, delta);
                }
                if (saveTimer > 0) {
                    String msg = saveSuccess ? "Saved!" : "Error!";
                    int color = saveSuccess ? Theme.GREEN : Theme.RED;
                    graphics.text(fontRenderer, FontHelper.comp(msg),
                            winX + winW - FontHelper.width(fontRenderer, msg) - 90,
                            winY + 9, color, false);
                }
            }
            case Connection -> {
                boolean tokenExists = new java.io.File(System.getProperty("user.home"), Constants.TokenFileName).exists();
                graphics.text(fontRenderer, FontHelper.comp(tokenExists ? "Token file exists" : "Token file not found"),
                        contentX(), winY + Theme.TITLE_BAR_H + Theme.TAB_BAR_H + 10,
                        tokenExists ? Theme.GREEN : Theme.YELLOW, false);
                txtToken.renderButton(graphics);
            }
            case Settings -> {
                graphics.text(fontRenderer, FontHelper.comp("Configuration"),
                        contentX(), winY + Theme.TITLE_BAR_H + Theme.TAB_BAR_H + 5, Theme.TEXT_PRIMARY, false);
            }
        }

        renderAllButtons(graphics, mouseX, mouseY, delta);
    }

    private void renderAllButtons(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (btnClose != null) btnClose.drawButton(graphics, mouseX, mouseY, delta);
        if (btnHelp != null) btnHelp.drawButton(graphics, mouseX, mouseY, delta);
        if (btnExport != null) btnExport.drawButton(graphics, mouseX, mouseY, delta);
        if (btnImport != null) btnImport.drawButton(graphics, mouseX, mouseY, delta);
        if (tabDashboard != null) tabDashboard.drawButton(graphics, mouseX, mouseY, delta);
        if (tabTriggers != null) tabTriggers.drawButton(graphics, mouseX, mouseY, delta);
        if (tabConnection != null) tabConnection.drawButton(graphics, mouseX, mouseY, delta);
        if (tabSettings != null) tabSettings.drawButton(graphics, mouseX, mouseY, delta);
        if (subDonate != null) subDonate.drawButton(graphics, mouseX, mouseY, delta);
        if (subSubscribe != null) subSubscribe.drawButton(graphics, mouseX, mouseY, delta);
        if (subTwitch != null) subTwitch.drawButton(graphics, mouseX, mouseY, delta);
        if (btnSave != null) btnSave.drawButton(graphics, mouseX, mouseY, delta);
        if (btnAdd != null) btnAdd.drawButton(graphics, mouseX, mouseY, delta);
        if (btnConnect != null) btnConnect.drawButton(graphics, mouseX, mouseY, delta);
        if (btnTokenSave != null) btnTokenSave.drawButton(graphics, mouseX, mouseY, delta);
        if (btnTokenDelete != null) btnTokenDelete.drawButton(graphics, mouseX, mouseY, delta);
        if (chkSkipTest != null) chkSkipTest.drawButton(graphics, mouseX, mouseY, delta);
    }

    private int contentX() { return winX + Theme.PADDING; }

    private String getVersionSuffix() { return "1.2"; }

    private int getTriggerCount() {
        if (currentSettings == null || currentSettings.triggers == null) return 0;
        int count = 0;
        for (var t : currentSettings.triggers)
            if (t != null && t.isActive) count++;
        return count;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (!initialized) return super.charTyped(event);
        if (activeTab == Tab.Triggers) {
            txtSearch.charTyped(event);
            switch (activeSubTab) {
                case Donate -> donatePanel.charTyped(event);
                case Subscribe -> subscribePanel.charTyped(event);
                case Twitch -> twitchPanel.charTyped(event);
            }
        } else if (activeTab == Tab.Connection) {
            txtToken.charTyped(event);
        }
        return true;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (!initialized) return super.keyPressed(event);
        if (activeTab == Tab.Triggers) {
            txtSearch.keyPressed(event);
            switch (activeSubTab) {
                case Donate -> donatePanel.keyPressed(event);
                case Subscribe -> subscribePanel.keyPressed(event);
                case Twitch -> twitchPanel.keyPressed(event);
            }
        } else if (activeTab == Tab.Connection) {
            txtToken.keyPressed(event);
        }
        if (event.key() == 256) return super.keyPressed(event);
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (!initialized) return super.mouseClicked(event, doubleClick);
        super.mouseClicked(event, doubleClick);
        if (activeTab == Tab.Triggers) {
            txtSearch.mouseClicked(event, doubleClick);
            switch (activeSubTab) {
                case Donate -> donatePanel.mouseClicked(event, doubleClick);
                case Subscribe -> subscribePanel.mouseClicked(event, doubleClick);
                case Twitch -> twitchPanel.mouseClicked(event, doubleClick);
            }
        } else if (activeTab == Tab.Connection) {
            txtToken.mouseClicked(event, doubleClick);
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
        if (activeTab == Tab.Dashboard) messagesPanel.mouseScrolled(mouseX, mouseY, deltaY);
        if (activeTab == Tab.Triggers) {
            switch (activeSubTab) {
                case Donate -> donatePanel.mouseScrolled(mouseX, mouseY, deltaY);
                case Subscribe -> subscribePanel.mouseScrolled(mouseX, mouseY, deltaY);
                case Twitch -> twitchPanel.mouseScrolled(mouseX, mouseY, deltaY);
            }
        }
        return true;
    }

    private void switchTab(Tab tab) {
        activeTab = tab;
        updateVisibility();
    }

    private void switchSubTab(TriggerSubTab tab) {
        activeSubTab = tab;
        updateVisibility();
    }

    private void doClose() { Minecraft.getInstance().setScreen(null); }

    private void doHelp() {
        try { if (Desktop.isDesktopSupported()) Desktop.getDesktop().browse(new URI(Constants.GuideToConfiguration)); }
        catch (Exception e) { e.printStackTrace(); }
    }

    private void doExport() {
        try {
            if (configurationSource != null && currentSettings != null)
                configurationSource.save(currentSettings);
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void doImport() {
        try {
            var home = System.getProperty("user.home");
            var dir = new java.io.File(home, "donation-alerts-integrate");
            var file = new java.io.File(dir, "settings.yaml");
            if (file.exists()) {
                var content = java.nio.file.Files.readString(file.toPath());
                var transformer = new net.bummy1337.daintegrate.configurations.YamlSettingsTransformer();
                var imported = transformer.transform(content);
                if (imported != null && imported.triggers != null) {
                    currentSettings.triggers = imported.triggers;
                    configurationSource.save(currentSettings);
                    rebuildPanels();
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void rebuildPanels() {
        donatePanel.clearEntries();
        subscribePanel.clearEntries();
        twitchPanel.clearEntries();
        if (currentSettings != null && currentSettings.triggers != null) {
            for (var trigger : currentSettings.triggers) {
                var kind = DonationTypeEntry.detectKind(trigger);
                switch (kind) {
                    case Subscribe -> subscribePanel.addEntry(new DonationTypeEntry(subscribePanel, trigger, mc, winX + winW - Theme.PADDING, DonationTypeEntry.TriggerKind.Subscribe));
                    case Twitch -> twitchPanel.addEntry(new DonationTypeEntry(twitchPanel, trigger, mc, winX + winW - Theme.PADDING, DonationTypeEntry.TriggerKind.Twitch));
                    default -> donatePanel.addEntry(new DonationTypeEntry(donatePanel, trigger, mc, winX + winW - Theme.PADDING, DonationTypeEntry.TriggerKind.Donate));
                }
            }
        }
    }

    private void settingsSkipTestDonationClick() {
        chkSkipTest.SwitchFlag(!chkSkipTest.Flag);
        currentSettings.skipTestDonation = chkSkipTest.Flag;
        try { configurationSource.save(currentSettings); } catch (Exception e) { e.printStackTrace(); }
    }

    private void statusSaveClick() {
        try {
            var home = System.getProperty("user.home");
            var writer = new PrintWriter(new java.io.File(home, Constants.TokenFileName), StandardCharsets.UTF_8);
            writer.println(txtToken.getText());
            writer.close();
        } catch (IOException e) { e.printStackTrace(); }
    }

    private void statusDeleteClick() {
        try {
            var home = System.getProperty("user.home");
            var file = new java.io.File(home, Constants.TokenFileName);
            if (file.exists()) file.delete();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void connectionControllerClick() {
        if (client.getConnected()) {
            client.disconnect();
            btnConnect.setMessage(Component.literal("Connect"));
        } else {
            var home = System.getProperty("user.home");
            var token = "";
            try (BufferedReader br = new BufferedReader(new FileReader(new java.io.File(home, Constants.TokenFileName)))) {
                String line;
                while ((line = br.readLine()) != null && line.length() > 3)
                    token = line.trim();
            } catch (FileNotFoundException e) { return; }
            catch (IOException e) { e.printStackTrace(); return; }
            if (!token.isEmpty()) client.connect(token);
            btnConnect.setMessage(Component.literal("Disconnect"));
        }
    }

    private void typesSaveClick() {
        try {
            List<TriggerDto> newTriggers = new ArrayList<>();
            for (var entry : donatePanel.getEntries()) newTriggers.add(buildTriggerDto(entry));
            for (var entry : subscribePanel.getEntries()) newTriggers.add(buildTriggerDto(entry));
            for (var entry : twitchPanel.getEntries()) newTriggers.add(buildTriggerDto(entry));
            currentSettings.triggers = newTriggers.toArray(new TriggerDto[0]);
            configurationSource.save(currentSettings);
            saveTimer = 120;
            saveSuccess = true;
        } catch (Exception e) {
            e.printStackTrace();
            saveTimer = 120;
            saveSuccess = false;
        }
    }

    private TriggerDto buildTriggerDto(DonationTypeEntry entry) {
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
        trigger.sensitives = new SensitivePropertiesDto[]{buildSensitive(entry)};
        return trigger;
    }

    private SensitivePropertiesDto buildSensitive(DonationTypeEntry entry) {
        var sensitive = new SensitivePropertiesDto();
        sensitive.properties = new PropertiesDto();
        switch (entry.getKind()) {
            case Donate -> {
                sensitive.properties.type = Constants.ModId + "/sensitive/donate";
                var p = new DonateSensitiveProperties();
                try { p.from = Float.parseFloat(entry.getFrom()); } catch (NumberFormatException e) { p.from = 0; }
                try { p.to = Float.parseFloat(entry.getTo()); } catch (NumberFormatException e) { p.to = 999999; }
                p.currency = entry.getCurrency();
                sensitive.properties.value = p;
            }
            case Subscribe -> {
                sensitive.properties.type = Constants.ModId + "/sensitive/subscribe";
                var p = new SubscribeSensitiveProperties();
                p.type = entry.getSubscribeType();
                sensitive.properties.value = p;
            }
            case Twitch -> {
                if ("Bits".equals(entry.getTwitchSubType())) {
                    sensitive.properties.type = Constants.ModId + "/sensitive/twitch/bits";
                    var p = new TwitchBitsSensitiveProperties();
                    try { p.from = Float.parseFloat(entry.getFrom()); } catch (NumberFormatException e) { p.from = 0; }
                    try { p.to = Float.parseFloat(entry.getTo()); } catch (NumberFormatException e) { p.to = 999999; }
                    sensitive.properties.value = p;
                } else {
                    sensitive.properties.type = Constants.ModId + "/sensitive/twitch/points";
                    var p = new TwitchPointsSensitiveProperties();
                    try { p.from = Float.parseFloat(entry.getFrom()); } catch (NumberFormatException e) { p.from = 0; }
                    try { p.to = Float.parseFloat(entry.getTo()); } catch (NumberFormatException e) { p.to = 999999; }
                    sensitive.properties.value = p;
                }
            }
        }
        return sensitive;
    }

    private void typesAddClick() {
        var newTrigger = new TriggerDto();
        newTrigger.name = "New Trigger";
        newTrigger.isActive = true;
        ScrollPanel<DonationTypeEntry> target;
        DonationTypeEntry.TriggerKind kind;
        switch (activeSubTab) {
            case Subscribe -> {
                kind = DonationTypeEntry.TriggerKind.Subscribe;
                target = subscribePanel;
                var p = new SubscribeSensitiveProperties();
                p.type = "YouTubeSubscription";
                var sp = new SensitivePropertiesDto();
                sp.properties = new PropertiesDto();
                sp.properties.type = Constants.ModId + "/sensitive/subscribe";
                sp.properties.value = p;
                newTrigger.sensitives = new SensitivePropertiesDto[]{sp};
            }
            case Twitch -> {
                kind = DonationTypeEntry.TriggerKind.Twitch;
                target = twitchPanel;
                var p = new TwitchPointsSensitiveProperties();
                p.from = 0; p.to = 999999;
                var sp = new SensitivePropertiesDto();
                sp.properties = new PropertiesDto();
                sp.properties.type = Constants.ModId + "/sensitive/twitch/points";
                sp.properties.value = p;
                newTrigger.sensitives = new SensitivePropertiesDto[]{sp};
            }
            default -> {
                kind = DonationTypeEntry.TriggerKind.Donate;
                target = donatePanel;
                var p = new DonateSensitiveProperties();
                p.from = 0; p.to = 999999; p.currency = "";
                var sp = new SensitivePropertiesDto();
                sp.properties = new PropertiesDto();
                sp.properties.type = Constants.ModId + "/sensitive/donate";
                sp.properties.value = p;
                newTrigger.sensitives = new SensitivePropertiesDto[]{sp};
            }
        }
        target.addEntry(new DonationTypeEntry(target, newTrigger, mc, winX + winW - Theme.PADDING, kind));
    }

    @Override
    public void onClose() { Minecraft.getInstance().setScreen(null); }

    @Override
    public boolean isPauseScreen() { return false; }
}
