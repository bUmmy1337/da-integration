package net.bummy1337.daintegrate.fabric.gui.clickgui;

import net.bummy1337.daintegrate.Constants;
import net.bummy1337.daintegrate.configurations.HandlerPropertiesDto;
import net.bummy1337.daintegrate.configurations.PropertiesDto;
import net.bummy1337.daintegrate.configurations.SensitivePropertiesDto;
import net.bummy1337.daintegrate.configurations.SettingsDto;
import net.bummy1337.daintegrate.configurations.TriggerDto;
import net.bummy1337.daintegrate.configurations.sources.FileConfigurationSource;
import net.bummy1337.daintegrate.fabric.gui.CustomButton;
import net.bummy1337.daintegrate.fabric.gui.CustomTextBox;
import net.bummy1337.daintegrate.fabric.gui.DonationTypeEntry;
import net.bummy1337.daintegrate.fabric.gui.ScrollPanel;
import net.bummy1337.daintegrate.fabric.gui.Theme;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.DragHandler;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.RenderHelper;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.background.BackgroundComponent;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.trigger.TriggerComponent;
import net.bummy1337.daintegrate.handlers.CommandHandlerProperties;
import net.bummy1337.daintegrate.handlers.MessageHandlerProperties;
import net.bummy1337.daintegrate.sensitives.DonateSensitiveProperties;
import net.bummy1337.daintegrate.sensitives.SubscribeSensitiveProperties;
import net.bummy1337.daintegrate.sensitives.TwitchBitsSensitiveProperties;
import net.bummy1337.daintegrate.sensitives.TwitchPointsSensitiveProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import net.bummy1337.dontaionalerts.DonationAlertsClient;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;
import org.lwjgl.glfw.GLFW;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClickGui extends Screen {
    private static ClickGui INSTANCE = new ClickGui();
    private static final int FIXED_GUI_SCALE = 2;

    private final BackgroundComponent background = new BackgroundComponent();
    private final TriggerComponent triggerComponent = new TriggerComponent();
    private final DragHandler dragHandler = new DragHandler();
    private final ScrollPanel<DonationTypeEntry> editorPanel = new ScrollPanel<>(0, 0, 1, 1);

    private String selectedCategory = "Donate";
    private TriggerDto editorTrigger = null;

    private float openAnimation = 0f;
    private boolean closing = false;

    private float hintAlphaAnimation = 0f;
    private long lastHintUpdateTime = System.currentTimeMillis();
    private static final float HINT_ANIM_SPEED = 6f;
    private static final float OFFSET_THRESHOLD = 5f;

    private static final long ANIM_DURATION_MS = 250;
    private long animStartTime = 0;
    private boolean animForward = true;

    private FileConfigurationSource configurationSource;
    private DonationAlertsClient client;
    private List<ReadOnlyDonationAlertsEvent> donations = new ArrayList<>();
    private CustomTextBox txtToken;
    private CustomButton btnConnect;
    private CustomButton btnTokenSave;
    private CustomButton btnTokenDelete;

    public static final int BG_WIDTH = BackgroundComponent.BG_WIDTH;
    public static final int BG_HEIGHT = BackgroundComponent.BG_HEIGHT;

    private static final float ML_X_OFFSET = 92f;
    private static final float ML_Y_OFFSET = 38f;
    private static final float ML_WIDTH = 120f;
    private static final float SP_X_OFFSET = 218f;
    private static final float SP_Y_OFFSET = 38f;
    private static final float SP_WIDTH = 394f;

    private static final float ACTION_Y_OFFSET = 326f;
    private static final float ACTION_BUTTON_W = 54f;
    private static final float ACTION_BUTTON_H = 18f;

    public ClickGui() {
        super(Component.literal("ClickGui"));
    }

    public static ClickGui getInstance() {
        return INSTANCE;
    }

    public void setConfigurationSource(FileConfigurationSource source) {
        this.configurationSource = source;
    }

    public void setContext(FileConfigurationSource source, DonationAlertsClient client, List<ReadOnlyDonationAlertsEvent> donations) {
        this.configurationSource = source;
        this.client = client;
        this.donations = donations != null ? donations : new ArrayList<>();
    }

    public boolean isClosing() {
        return closing;
    }

    @Override
    protected void init() {
        super.init();
        closing = false;
        startAnimation(true);
        hintAlphaAnimation = 0f;
        lastHintUpdateTime = System.currentTimeMillis();
        RenderHelper.setFont(Minecraft.getInstance().font);

        Minecraft mc = Minecraft.getInstance();
        long handle = mc.getWindow().handle();
        double centerX = mc.getWindow().getWidth() / 2.0;
        double centerY = mc.getWindow().getHeight() / 2.0;
        GLFW.glfwSetCursorPos(handle, centerX, centerY);

        background.setSearchActive(false);
        initConnectionWidgets();
        updateTriggers();
    }

    private void initConnectionWidgets() {
        Minecraft mc = Minecraft.getInstance();
        txtToken = new CustomTextBox(mc.font, 0, 0, 260, 20, "");
        txtToken.tag = "Token";
        btnConnect = new CustomButton(0, 0, 84, 18, client != null && client.getConnected() ? "Disconnect" : "Connect", this::connectionControllerClick);
        btnTokenSave = new CustomButton(0, 0, 84, 18, "Save Token", this::statusSaveClick);
        btnTokenDelete = new CustomButton(0, 0, 84, 18, "Delete", this::statusDeleteClick);
        for (CustomButton button : new CustomButton[]{btnConnect, btnTokenSave, btnTokenDelete}) {
            button.CornerRadius = 6;
            button.DefaultBackgroundColor = Theme.BG_ENTRY;
            button.HoveredBackgroundColor = Theme.BG_ENTRY_HOVER;
            button.DefaultForegroundColor = Theme.TEXT_SECONDARY;
            button.HoveredForegroundColor = Theme.TEXT_PRIMARY;
        }
    }

    private void updateTriggers() {
        List<TriggerDto> triggers = new ArrayList<>();
        try {
            if (configurationSource != null) {
                SettingsDto settings = configurationSource.getCurrent();
                if (settings != null && settings.triggers != null) {
                    for (TriggerDto t : settings.triggers) {
                        if (t != null && selectedCategory.equals(kindName(DonationTypeEntry.detectKind(t)))) {
                            triggers.add(t);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
        triggerComponent.updateTriggers(triggers, selectedCategory);

        List<TriggerDto> allTriggers = new ArrayList<>();
        try {
            if (configurationSource != null) {
                SettingsDto settings = configurationSource.getCurrent();
                if (settings != null && settings.triggers != null) {
                    allTriggers.addAll(Arrays.asList(settings.triggers));
                }
            }
        } catch (Exception ignored) {
        }
        background.getSearchHandler().setAllTriggers(allTriggers);
    }

    private String kindName(DonationTypeEntry.TriggerKind kind) {
        return switch (kind) {
            case Subscribe -> "Subscribe";
            case Twitch -> "Twitch";
            default -> "Donate";
        };
    }

    private DonationTypeEntry.TriggerKind currentKind() {
        return switch (selectedCategory) {
            case "Subscribe" -> DonationTypeEntry.TriggerKind.Subscribe;
            case "Twitch" -> DonationTypeEntry.TriggerKind.Twitch;
            default -> DonationTypeEntry.TriggerKind.Donate;
        };
    }

    private boolean isTriggerSection() {
        return "Donate".equals(selectedCategory) || "Subscribe".equals(selectedCategory) || "Twitch".equals(selectedCategory);
    }

    private void openEditor(TriggerDto trigger) {
        if (trigger == null) return;
        editorTrigger = trigger;
        editorPanel.clearEntries();
        DonationTypeEntry entry = new DonationTypeEntry(editorPanel, trigger, Minecraft.getInstance(), 1, DonationTypeEntry.detectKind(trigger));
        entry.setShowActiveToggle(false);
        editorPanel.addEntry(entry);
        editorPanel.updateHeight();
    }

    private void addTrigger() {
        TriggerDto trigger = new TriggerDto();
        trigger.name = "New " + selectedCategory + " Trigger";
        trigger.description = "";
        trigger.isActive = true;
        trigger.sensitives = new SensitivePropertiesDto[]{buildDefaultSensitive(currentKind())};
        trigger.handlers = new HandlerPropertiesDto[0];

        SettingsDto settings = currentSettings();
        List<TriggerDto> list = new ArrayList<>();
        if (settings.triggers != null) {
            list.addAll(Arrays.asList(settings.triggers));
        }
        list.add(trigger);
        settings.triggers = list.toArray(new TriggerDto[0]);
        saveSettings(settings);
        updateTriggers();
        triggerComponent.selectTrigger(trigger);
        openEditor(trigger);
    }

    private SettingsDto currentSettings() {
        SettingsDto settings = configurationSource != null ? configurationSource.getCurrent() : null;
        return settings != null ? settings : new SettingsDto();
    }

    private void saveSettings(SettingsDto settings) {
        try {
            if (configurationSource != null) {
                configurationSource.save(settings);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void saveEditor() {
        if (editorTrigger == null) return;

        SettingsDto settings = currentSettings();
        List<TriggerDto> triggers = new ArrayList<>();
        if (settings.triggers != null) {
            triggers.addAll(Arrays.asList(settings.triggers));
        }

        if (editorPanel.getEntries().isEmpty()) {
            triggers.remove(editorTrigger);
            editorTrigger = null;
        } else {
            TriggerDto updated = buildTriggerDto(editorPanel.getEntries().get(0));
            int index = triggers.indexOf(editorTrigger);
            if (index >= 0) {
                triggers.set(index, updated);
            } else {
                triggers.add(updated);
            }
            editorTrigger = updated;
            openEditor(updated);
        }

        settings.triggers = triggers.toArray(new TriggerDto[0]);
        saveSettings(settings);
        updateTriggers();
    }

    private TriggerDto buildTriggerDto(DonationTypeEntry entry) {
        TriggerDto trigger = new TriggerDto();
        trigger.name = entry.getName();
        trigger.description = editorTrigger != null ? editorTrigger.description : "";
        trigger.isActive = editorTrigger != null ? editorTrigger.isActive : true;

        List<HandlerPropertiesDto> handlers = new ArrayList<>();
        for (String msg : entry.getMessages()) {
            HandlerPropertiesDto hp = new HandlerPropertiesDto();
            MessageHandlerProperties props = new MessageHandlerProperties();
            props.message = msg;
            hp.properties = new PropertiesDto<>();
            hp.properties.type = Constants.ModId + "/handler/message";
            hp.properties.value = props;
            hp.delay = 0;
            handlers.add(hp);
        }
        for (String cmd : entry.getCommands()) {
            HandlerPropertiesDto hp = new HandlerPropertiesDto();
            CommandHandlerProperties props = new CommandHandlerProperties();
            props.command = cmd;
            hp.properties = new PropertiesDto<>();
            hp.properties.type = Constants.ModId + "/handler/command";
            hp.properties.value = props;
            hp.delay = 0;
            handlers.add(hp);
        }

        trigger.handlers = handlers.toArray(new HandlerPropertiesDto[0]);
        trigger.sensitives = new SensitivePropertiesDto[]{buildSensitive(entry)};
        return trigger;
    }

    private SensitivePropertiesDto buildDefaultSensitive(DonationTypeEntry.TriggerKind kind) {
        TriggerDto wrapper = new TriggerDto();
        wrapper.name = "New Trigger";
        DonationTypeEntry entry = new DonationTypeEntry(editorPanel, wrapper, Minecraft.getInstance(), 1, kind);
        return buildSensitive(entry);
    }

    private SensitivePropertiesDto buildSensitive(DonationTypeEntry entry) {
        SensitivePropertiesDto sensitive = new SensitivePropertiesDto();
        sensitive.properties = new PropertiesDto();
        switch (entry.getKind()) {
            case Subscribe -> {
                sensitive.properties.type = Constants.ModId + "/sensitive/subscribe";
                SubscribeSensitiveProperties p = new SubscribeSensitiveProperties();
                p.type = entry.getSubscribeType();
                sensitive.properties.value = p;
            }
            case Twitch -> {
                boolean bits = "Bits".equals(entry.getTwitchSubType());
                sensitive.properties.type = Constants.ModId + (bits ? "/sensitive/twitch/bits" : "/sensitive/twitch/points");
                if (bits) {
                    TwitchBitsSensitiveProperties p = new TwitchBitsSensitiveProperties();
                    p.from = parseFloat(entry.getFrom(), 0);
                    p.to = parseFloat(entry.getTo(), 999999);
                    sensitive.properties.value = p;
                } else {
                    TwitchPointsSensitiveProperties p = new TwitchPointsSensitiveProperties();
                    p.from = parseFloat(entry.getFrom(), 0);
                    p.to = parseFloat(entry.getTo(), 999999);
                    sensitive.properties.value = p;
                }
            }
            default -> {
                sensitive.properties.type = Constants.ModId + "/sensitive/donate";
                DonateSensitiveProperties p = new DonateSensitiveProperties();
                p.from = parseFloat(entry.getFrom(), 0);
                p.to = parseFloat(entry.getTo(), 999999);
                p.currency = entry.getCurrency();
                sensitive.properties.value = p;
            }
        }
        return sensitive;
    }

    private float parseFloat(String value, float fallback) {
        try {
            return Float.parseFloat(value);
        } catch (Exception ignored) {
            return fallback;
        }
    }

    public void openGui() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen == null) {
            closing = false;
            startAnimation(true);
            mc.setScreen(this);
        }
    }

    private void startAnimation(boolean forward) {
        animForward = forward;
        animStartTime = System.currentTimeMillis();
        if (forward) {
            openAnimation = 0f;
        }
    }

    private void updateOpenAnimation() {
        long elapsed = System.currentTimeMillis() - animStartTime;
        float progress = Math.min(1f, elapsed / (float) ANIM_DURATION_MS);
        progress = easeOutCubic(progress);

        if (animForward) {
            openAnimation = progress;
        } else {
            openAnimation = 1f - progress;
        }
    }

    private float easeOutCubic(float x) {
        return 1f - (float) Math.pow(1 - x, 3);
    }

    @Override
    public void tick() {
        super.tick();
    }

    private float[] calculateBackground() {
        Minecraft mc = Minecraft.getInstance();
        int vw = mc.getWindow().getGuiScaledWidth();
        int vh = mc.getWindow().getGuiScaledHeight();
        float bgX = (vw - BG_WIDTH) / 2f + dragHandler.getOffsetX();
        float bgY = (vh - BG_HEIGHT) / 2f + dragHandler.getOffsetY();
        return new float[]{bgX, bgY};
    }

    private void updateHintAnimation() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((currentTime - lastHintUpdateTime) / 1000f, 0.1f);
        lastHintUpdateTime = currentTime;

        float offsetX = Math.abs(dragHandler.getOffsetX());
        float offsetY = Math.abs(dragHandler.getOffsetY());
        boolean shouldShow = (offsetX > OFFSET_THRESHOLD || offsetY > OFFSET_THRESHOLD);

        float target = shouldShow ? 1f : 0f;
        float diff = target - hintAlphaAnimation;

        if (Math.abs(diff) < 0.001f) {
            hintAlphaAnimation = target;
        } else {
            hintAlphaAnimation += diff * HINT_ANIM_SPEED * deltaTime;
            hintAlphaAnimation = Math.max(0f, Math.min(1f, hintAlphaAnimation));
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        updateOpenAnimation();

        Minecraft mc = Minecraft.getInstance();
        float animValue = openAnimation;

        if (closing && animValue <= 0f) {
            closing = false;
            dragHandler.stopDrag();
            mc.setScreen(null);
            return;
        }

        int dimAlpha = (int) (125 * animValue);
        if (dimAlpha > 0) {
            g.fill(0, 0, width, height, (dimAlpha << 24));
        }

        float scale = (float) FIXED_GUI_SCALE / Math.max(1, mc.getWindow().getGuiScale());
        float mx = mouseX / scale;
        float my = mouseY / scale;

        if (!closing) {
            dragHandler.update(mx, my);
        }

        updateHintAnimation();

        float[] bg = calculateBackground();
        float bgX = bg[0];
        float bgY = bg[1];

        float yOffset;
        if (closing) {
            yOffset = (1f - animValue) * 30f;
        } else {
            yOffset = (1f - animValue) * -15f;
        }
        bgY += yOffset;

        float alphaMultiplier = animValue;

        float mlX = bgX + ML_X_OFFSET;
        float mlY = bgY + ML_Y_OFFSET;
        float mlW = ML_WIDTH;
        float mlH = BG_HEIGHT - 46f;
        float spX = bgX + SP_X_OFFSET;
        float spY = bgY + SP_Y_OFFSET;
        float spW = SP_WIDTH;
        float spH = BG_HEIGHT - 46f;

        float scrollSpeed = 1f;

        float normalAlpha = background.getNormalPanelAlpha();
        float searchAlpha = background.getSearchPanelAlpha();

        background.render(g, bgX, bgY, selectedCategory, delta, alphaMultiplier);
        background.renderCategoryPanel(g, bgX, bgY, alphaMultiplier);
        background.renderHeader(g, bgX, bgY, selectedCategory, alphaMultiplier);
        background.renderCategoryNames(g, bgX, bgY, selectedCategory, alphaMultiplier);

        if (normalAlpha > 0.01f && isTriggerSection()) {
            triggerComponent.updateScroll(delta, scrollSpeed);
            triggerComponent.updateScrollFades(delta, scrollSpeed, mlH, spH);
            triggerComponent.renderTriggerList(g, mlX, mlY, mlW, mlH, mx, my, FIXED_GUI_SCALE, alphaMultiplier * normalAlpha);
            renderEditorPanel(g, spX, spY, spW, spH, mx, my, delta, alphaMultiplier * normalAlpha);
        }

        if (normalAlpha > 0.01f && "Dashboard".equals(selectedCategory)) {
            renderDashboardPanel(g, bgX + ML_X_OFFSET, bgY + ML_Y_OFFSET, BG_WIDTH - 100f, BG_HEIGHT - 46f,
                    alphaMultiplier * normalAlpha);
        }

        if (normalAlpha > 0.01f && "Connection".equals(selectedCategory)) {
            renderConnectionPanel(g, bgX + ML_X_OFFSET, bgY + ML_Y_OFFSET, BG_WIDTH - 100f, BG_HEIGHT - 46f,
                    mx, my, delta, alphaMultiplier * normalAlpha);
        }

        if (normalAlpha > 0.01f && "Help".equals(selectedCategory)) {
            renderHelpPanel(g, bgX + ML_X_OFFSET, bgY + ML_Y_OFFSET, BG_WIDTH - 100f, BG_HEIGHT - 46f,
                    alphaMultiplier * normalAlpha);
        }

        if (searchAlpha > 0.01f) {
            background.renderSearchResults(g, bgX, bgY, mx, my, FIXED_GUI_SCALE, alphaMultiplier);
        }

        float finalHintAlpha = hintAlphaAnimation * alphaMultiplier;
        if (finalHintAlpha > 0.01f) {
            int hintAlpha = (int) (255 * finalHintAlpha);
            float textX = width / 2f - 80;
            float textY = height / 2f + BG_HEIGHT / 2f + 75f;
            int textColor = Theme.withAlpha(Theme.TEXT_MUTED, hintAlpha);
            g.text(mc.font, Component.literal("Press CTRL + ALT to reset position"),
                    (int) textX, (int) textY, textColor, false);
        }
    }

    private void renderEditorPanel(GuiGraphicsExtractor g, float x, float y, float width, float height,
                                   float mouseX, float mouseY, float delta, float alphaMultiplier) {
        RenderHelper.roundedRect(g, x, y, width, height, 8f, Theme.withAlpha(Theme.BG_PANEL, (int) (64 * alphaMultiplier)));
        RenderHelper.roundedOutline(g, x, y, width, height, 8f, Theme.withAlpha(Theme.BORDER, (int) (190 * alphaMultiplier)));

        RenderHelper.text(g, editorTrigger == null ? "Trigger editor" : "Editing trigger",
                x + 10, y + 9, 7, Theme.withAlpha(Theme.TEXT_PRIMARY, (int) (220 * alphaMultiplier)));

        renderActionButton(g, x + width - ACTION_BUTTON_W * 2 - 18, y + 7, ACTION_BUTTON_W, ACTION_BUTTON_H,
                "Add", isActionHovered(mouseX, mouseY, x + width - ACTION_BUTTON_W * 2 - 18, y + 7, ACTION_BUTTON_W, ACTION_BUTTON_H), alphaMultiplier);
        renderActionButton(g, x + width - ACTION_BUTTON_W - 10, y + 7, ACTION_BUTTON_W, ACTION_BUTTON_H,
                "Save", isActionHovered(mouseX, mouseY, x + width - ACTION_BUTTON_W - 10, y + 7, ACTION_BUTTON_W, ACTION_BUTTON_H), alphaMultiplier);

        int panelX = (int) (x + 8);
        int panelY = (int) (y + 32);
        int panelRight = (int) (x + width - 8);
        int panelBottom = (int) (y + height - 8);
        editorPanel.setBounds(panelX, panelY, panelRight, panelBottom);

        if (editorPanel.getEntries().isEmpty()) {
            String hint = "Right-click trigger to edit or press Add";
            float hintW = RenderHelper.textWidth(hint, 6f);
            RenderHelper.text(g, hint, x + (width - hintW) / 2f, y + height / 2f,
                    6f, Theme.withAlpha(Theme.TEXT_MUTED, (int) (160 * alphaMultiplier)));
            return;
        }

        for (DonationTypeEntry entry : editorPanel.getEntries()) {
            entry.setRight(panelRight);
        }
        editorPanel.drawPanel(g, (int) mouseX, (int) mouseY, delta);
    }

    private void renderActionButton(GuiGraphicsExtractor g, float x, float y, float width, float height,
                                    String label, boolean hovered, float alphaMultiplier) {
        int bg = hovered ? Theme.BG_ENTRY_HOVER : Theme.BG_ENTRY;
        int border = hovered ? Theme.BORDER_FOCUS : Theme.BORDER;
        RenderHelper.roundedRect(g, x, y, width, height, 6f, Theme.withAlpha(bg, (int) (210 * alphaMultiplier)));
        RenderHelper.roundedOutline(g, x, y, width, height, 6f, Theme.withAlpha(border, (int) (220 * alphaMultiplier)));
        float textW = RenderHelper.textWidth(label, 6f);
        RenderHelper.text(g, label, x + (width - textW) / 2f, y + 6f, 6f,
                Theme.withAlpha(Theme.TEXT_PRIMARY, (int) (220 * alphaMultiplier)));
    }

    private boolean isActionHovered(double mouseX, double mouseY, float x, float y, float w, float h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    private void renderDashboardPanel(GuiGraphicsExtractor g, float x, float y, float width, float height, float alphaMultiplier) {
        RenderHelper.roundedRect(g, x, y, width, height, 8f, Theme.withAlpha(Theme.BG_PANEL, (int) (80 * alphaMultiplier)));
        RenderHelper.roundedOutline(g, x, y, width, height, 8f, Theme.withAlpha(Theme.BORDER, (int) (190 * alphaMultiplier)));

        int triggerCount = 0;
        SettingsDto settings = currentSettings();
        if (settings.triggers != null) {
            for (TriggerDto trigger : settings.triggers) {
                if (trigger != null && trigger.isActive) triggerCount++;
            }
        }

        RenderHelper.text(g, "Dashboard", x + 12, y + 12, 8, Theme.withAlpha(Theme.TEXT_PRIMARY, (int) (230 * alphaMultiplier)));
        RenderHelper.text(g, "Active triggers: " + triggerCount, x + 12, y + 34, 6, Theme.withAlpha(Theme.TEXT_SECONDARY, (int) (190 * alphaMultiplier)));
        RenderHelper.text(g, client != null && client.getConnected() ? "Status: Connected" : "Status: Disconnected",
                x + 12, y + 48, 6, Theme.withAlpha(Theme.TEXT_SECONDARY, (int) (190 * alphaMultiplier)));

        float listY = y + 74;
        RenderHelper.text(g, "Recent events", x + 12, listY - 16, 6, Theme.withAlpha(Theme.TEXT_MUTED, (int) (180 * alphaMultiplier)));
        if (donations == null || donations.isEmpty()) {
            RenderHelper.text(g, "No events yet. Connect to start receiving.", x + 12, listY,
                    6, Theme.withAlpha(Theme.TEXT_MUTED, (int) (150 * alphaMultiplier)));
            return;
        }

        int shown = 0;
        for (int i = donations.size() - 1; i >= 0 && shown < 8; i--, shown++) {
            ReadOnlyDonationAlertsEvent event = donations.get(i);
            String line = event.getUserName() + " - " + event.getAmount() + " " + event.getCurrency();
            RenderHelper.roundedRect(g, x + 10, listY + shown * 24, width - 20, 19, 5f,
                    Theme.withAlpha(Theme.BG_ENTRY, (int) (160 * alphaMultiplier)));
            RenderHelper.text(g, line, x + 18, listY + 5 + shown * 24, 6,
                    Theme.withAlpha(Theme.TEXT_SECONDARY, (int) (205 * alphaMultiplier)));
        }
    }

    private void renderConnectionPanel(GuiGraphicsExtractor g, float x, float y, float width, float height,
                                       float mouseX, float mouseY, float delta, float alphaMultiplier) {
        RenderHelper.roundedRect(g, x, y, width, height, 8f, Theme.withAlpha(Theme.BG_PANEL, (int) (80 * alphaMultiplier)));
        RenderHelper.roundedOutline(g, x, y, width, height, 8f, Theme.withAlpha(Theme.BORDER, (int) (190 * alphaMultiplier)));

        RenderHelper.text(g, "Connection", x + 12, y + 12, 8, Theme.withAlpha(Theme.TEXT_PRIMARY, (int) (230 * alphaMultiplier)));
        boolean connected = client != null && client.getConnected();
        RenderHelper.text(g, connected ? "Connected" : "Disconnected", x + 12, y + 34, 6,
                Theme.withAlpha(connected ? Theme.TEXT_PRIMARY : Theme.TEXT_MUTED, (int) (200 * alphaMultiplier)));

        if (txtToken != null) {
            txtToken.renderButton(g, (int) (x + 12), (int) (y + 62), delta);
        }
        if (btnConnect != null) {
            btnConnect.setMessage(Component.literal(connected ? "Disconnect" : "Connect"));
            btnConnect.drawButton(g, Minecraft.getInstance(), (int) (x + 12), (int) (y + 96), (int) mouseX, (int) mouseY, delta);
            btnTokenSave.drawButton(g, Minecraft.getInstance(), (int) (x + 104), (int) (y + 96), (int) mouseX, (int) mouseY, delta);
            btnTokenDelete.drawButton(g, Minecraft.getInstance(), (int) (x + 196), (int) (y + 96), (int) mouseX, (int) mouseY, delta);
        }
    }

    private void renderHelpPanel(GuiGraphicsExtractor g, float x, float y, float width, float height, float alphaMultiplier) {
        RenderHelper.roundedRect(g, x, y, width, height, 8f, Theme.withAlpha(Theme.BG_PANEL, (int) (80 * alphaMultiplier)));
        RenderHelper.roundedOutline(g, x, y, width, height, 8f, Theme.withAlpha(Theme.BORDER, (int) (190 * alphaMultiplier)));

        RenderHelper.text(g, "Help", x + 12, y + 12, 8, Theme.withAlpha(Theme.TEXT_PRIMARY, (int) (230 * alphaMultiplier)));
        RenderHelper.text(g, "Placeholders", x + 12, y + 38, 7, Theme.withAlpha(Theme.TEXT_SECONDARY, (int) (210 * alphaMultiplier)));

        String[] placeholders = {
                "{username} - donation author",
                "{amount} - donated amount",
                "{currency} - donation currency",
                "{message} - donation message",
                "{playername} - current Minecraft player"
        };

        for (int i = 0; i < placeholders.length; i++) {
            float itemY = y + 62 + i * 22;
            RenderHelper.roundedRect(g, x + 10, itemY, width - 20, 17, 5f,
                    Theme.withAlpha(Theme.BG_ENTRY, (int) (150 * alphaMultiplier)));
            RenderHelper.text(g, placeholders[i], x + 18, itemY + 5, 6,
                    Theme.withAlpha(Theme.TEXT_SECONDARY, (int) (205 * alphaMultiplier)));
        }
    }

    private void handleConnectionClick(MouseButtonEvent event, boolean doubleClick) {
        if (txtToken != null) txtToken.mouseClicked(event, doubleClick);
        if (btnConnect != null) btnConnect.mouseClicked(event, doubleClick);
        if (btnTokenSave != null) btnTokenSave.mouseClicked(event, doubleClick);
        if (btnTokenDelete != null) btnTokenDelete.mouseClicked(event, doubleClick);
    }

    private void statusSaveClick() {
        try {
            PrintWriter writer = new PrintWriter(new File(System.getProperty("user.home"), Constants.TokenFileName), StandardCharsets.UTF_8);
            writer.println(txtToken != null ? txtToken.getText() : "");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void statusDeleteClick() {
        File file = new File(System.getProperty("user.home"), Constants.TokenFileName);
        if (file.exists()) file.delete();
    }

    private void connectionControllerClick() {
        if (client == null) return;
        if (client.getConnected()) {
            client.disconnect();
            return;
        }
        String token = txtToken != null ? txtToken.getText().trim() : "";
        if (token.isEmpty()) {
            try (BufferedReader br = new BufferedReader(new FileReader(new File(System.getProperty("user.home"), Constants.TokenFileName)))) {
                String line;
                while ((line = br.readLine()) != null && line.length() > 3) {
                    token = line.trim();
                }
            } catch (FileNotFoundException ignored) {
                return;
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }
        if (!token.isEmpty()) client.connect(token);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (closing) return false;

        Minecraft mc = Minecraft.getInstance();
        int guiScale = Math.max(1, mc.getWindow().getGuiScale());
        float scale = (float) FIXED_GUI_SCALE / guiScale;
        double mx = event.x() / scale;
        double my = event.y() / scale;

        float[] bg = calculateBackground();
        float bgX = bg[0];
        float bgY = bg[1];

        if (background.isSearchBoxHovered(mx, my, bgX, bgY) && event.button() == 0) {
            background.setSearchActive(true);
            return true;
        }

        if (background.isSearchActive()) {
            if (event.button() == 0) {
                TriggerDto searchTrigger = background.getSearchTriggerAtPosition(mx, my, bgX, bgY);
                if (searchTrigger != null) {
                    searchTrigger.isActive = !searchTrigger.isActive;
                    saveSettings(currentSettings());
                    return true;
                }

                float panelX = bgX + ML_X_OFFSET;
                float panelY = bgY + ML_Y_OFFSET;
                float panelW = BG_WIDTH - 100f;
                float panelH = BG_HEIGHT - 46f;

                if (mx >= panelX && mx <= panelX + panelW && my >= panelY && my <= panelY + panelH) {
                    return true;
                }

                if (!background.isSearchBoxHovered(mx, my, bgX, bgY)) {
                    background.setSearchActive(false);
                }
            } else if (event.button() == 1) {
                TriggerDto searchTrigger = background.getSearchTriggerAtPosition(mx, my, bgX, bgY);
                if (searchTrigger != null) {
                    background.setSearchActive(false);
                    triggerComponent.selectTriggerFromSearch(searchTrigger);
                    updateTriggers();
                    return true;
                }
            }
            return true;
        }

        if (event.button() == 2) {
            if (dragHandler.startDrag(mx, my, bgX, bgY, BG_WIDTH, BG_HEIGHT)) {
                return true;
            }
        }

        String cat = background.getCategoryAtPosition(mx, my, bgX, bgY);
        if (cat != null) {
            selectedCategory = cat;
            background.setSearchActive(false);
            updateTriggers();
            return true;
        }

        float mlX = bgX + ML_X_OFFSET;
        float mlY = bgY + ML_Y_OFFSET;
        float mlW = ML_WIDTH;
        float mlH = BG_HEIGHT - 48f;

        TriggerDto trigger = isTriggerSection() ? triggerComponent.getTriggerAtPosition(mx, my, mlX, mlY, mlW, mlH) : null;
        if (trigger != null) {
            if (event.button() == 0) {
                trigger.isActive = !trigger.isActive;
                saveSettings(currentSettings());
            }
            else if (event.button() == 1) {
                triggerComponent.selectTrigger(trigger);
                openEditor(trigger);
            }
            return true;
        }

        float spX = bgX + SP_X_OFFSET;
        float spY = bgY + SP_Y_OFFSET;
        float spW = SP_WIDTH;
        float spH = BG_HEIGHT - 48f;
        if (isTriggerSection() && mx >= spX && mx <= spX + spW && my >= spY && my <= spY + spH) {
            float addX = spX + spW - ACTION_BUTTON_W * 2 - 18;
            float saveX = spX + spW - ACTION_BUTTON_W - 10;
            float actionY = spY + 7;
            if (event.button() == 0 && isActionHovered(mx, my, addX, actionY, ACTION_BUTTON_W, ACTION_BUTTON_H)) {
                addTrigger();
                return true;
            }
            if (event.button() == 0 && isActionHovered(mx, my, saveX, actionY, ACTION_BUTTON_W, ACTION_BUTTON_H)) {
                saveEditor();
                return true;
            }
            editorPanel.mouseClicked(new MouseButtonEvent(mx, my, event.buttonInfo()), doubleClick);
            if (editorTrigger != null && editorPanel.getEntries().isEmpty()) {
                saveEditor();
            }
            return true;
        }

        if ("Connection".equals(selectedCategory) && mx >= bgX + ML_X_OFFSET && mx <= bgX + BG_WIDTH - 8 && my >= bgY + ML_Y_OFFSET && my <= bgY + BG_HEIGHT - 8) {
            handleConnectionClick(new MouseButtonEvent(mx, my, event.buttonInfo()), doubleClick);
            return true;
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double deltaY) {
        if (closing) return false;

        Minecraft mc = Minecraft.getInstance();
        int guiScale = Math.max(1, mc.getWindow().getGuiScale());
        float scale = (float) FIXED_GUI_SCALE / guiScale;
        double mx = mouseX / scale;
        double my = mouseY / scale;

        float[] bg = calculateBackground();
        float bgX = bg[0];
        float bgY = bg[1];

        if (background.isSearchActive()) {
            float panelX = bgX + ML_X_OFFSET;
            float panelY = bgY + ML_Y_OFFSET;
            float panelW = BG_WIDTH - 100f;
            float panelH = BG_HEIGHT - 46f;

            if (mx >= panelX && mx <= panelX + panelW && my >= panelY && my <= panelY + panelH) {
                background.handleSearchScroll(deltaY, panelH);
                return true;
            }
        }

        float mlX = bgX + ML_X_OFFSET;
        float mlY = bgY + ML_Y_OFFSET;
        float mlW = ML_WIDTH;
        float mlH = BG_HEIGHT - 48f;
        if (mx >= mlX && mx <= mlX + mlW && my >= mlY && my <= mlY + mlH) {
            triggerComponent.handleTriggerScroll(deltaY, mlH);
            return true;
        }

        float spX = bgX + SP_X_OFFSET;
        float spY = bgY + SP_Y_OFFSET;
        float spW = SP_WIDTH;
        float spH = BG_HEIGHT - 48f;
        if (mx >= spX && mx <= spX + spW && my >= spY && my <= spY + spH) {
            editorPanel.mouseScrolled(mx, my, deltaY);
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, deltaY);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == GLFW.GLFW_KEY_ESCAPE) {
            if (background.isSearchActive()) {
                background.setSearchActive(false);
                return true;
            }
            onClose();
            return true;
        }

        if (closing) return false;

        if (background.isSearchActive()) {
            if (background.handleSearchKey(event.key())) {
                return true;
            }
        }

        if (!editorPanel.getEntries().isEmpty() && editorPanel.keyPressed(event)) {
            return true;
        }

        if ("Connection".equals(selectedCategory) && txtToken != null) {
            txtToken.keyPressed(event);
            return true;
        }

        if (dragHandler.isResetNeeded(event.key(), event.modifiers())) {
            dragHandler.reset();
            return true;
        }

        return super.keyPressed(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (closing) return false;

        if (background.isSearchActive()) {
            if (background.handleSearchChar((char) event.codepoint())) {
                return true;
            }
        }

        if (!editorPanel.getEntries().isEmpty()) {
            editorPanel.charTyped(event);
            return true;
        }

        if ("Connection".equals(selectedCategory) && txtToken != null) {
            txtToken.charTyped(event);
            return true;
        }

        return super.charTyped(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void onClose() {
        if (!closing) {
            closing = true;
            startAnimation(false);

            Minecraft mc = Minecraft.getInstance();
            long handle = mc.getWindow().handle();
            double centerX = mc.getWindow().getWidth() / 2.0;
            double centerY = mc.getWindow().getHeight() / 2.0;

            GLFW.glfwSetInputMode(handle, GLFW.GLFW_CURSOR, GLFW.GLFW_CURSOR_DISABLED);
            GLFW.glfwSetCursorPos(handle, centerX, centerY);

            background.setSearchActive(false);
            dragHandler.stopDrag();
        }
    }
}
