package net.bummy1337.daintegrate.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.bummy1337.daintegrate.Constants;
import net.bummy1337.daintegrate.DonationAlertsIntegrate;
import net.bummy1337.daintegrate.DonationAlertsIntegrateFactory;
import net.bummy1337.daintegrate.EventProcessor;
import net.bummy1337.daintegrate.TokenStore;
import net.bummy1337.daintegrate.configurations.sources.FileConfigurationSource;
import net.bummy1337.daintegrate.fabric.imgui.ImGuiScreen;
import net.bummy1337.daintegrate.listeners.DonationAlertsEventListener;
import net.bummy1337.daintegrate.ui.DaMenu;
import net.bummy1337.daintegrate.ui.I18n;
import net.bummy1337.dontaionalerts.ConnectionState;
import net.bummy1337.dontaionalerts.DonationAlertsClient;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ClientEntryPoint implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Constants.ModId);
    private static final int MaxEvents = 100;
    private static final int Orange = 0xFFAA00;
    private static final int Link = 0x55FFFF;
    private static final int Red = 0xFF5555;
    private static final int Green = 0x55FF55;

    private FileConfigurationSource configurationSource;
    private DonationAlertsClient client;
    private final TokenStore tokens = new TokenStore();
    private final List<ReadOnlyDonationAlertsEvent> events = new CopyOnWriteArrayList<>();
    private KeyMapping openKey;
    private MinecraftUiContext uiContext;
    private ConnectionState lastAnnounced = ConnectionState.Disconnected;

    @Override
    public void onInitializeClient() {
        DonationAlertsIntegrate.configure(Constants.ModId, Constants.ModUrl)
                .registerHandler(new MessageHandler())
                .registerHandler(new CommandHandler());

        try {
            configurationSource = DonationAlertsIntegrateFactory.create(
                    "donation-alerts-integrate",
                    "settings.yaml",
                    LOGGER::info);
            // Watch the file from the start, so the menu and the triggers always see the latest settings
            configurationSource.startListening();
        } catch (IOException e) {
            LOGGER.error("failed to initialize donation-alerts-integrate", e);
        }

        var eventProcessor = new EventProcessor();
        try {
            client = new DonationAlertsClient(Constants.DonationAlertsEventServer, new DonationAlertsEventListener(eventProcessor));
        } catch (java.net.URISyntaxException e) {
            throw new RuntimeException(e);
        }
        client.addStateListener(state -> Minecraft.getInstance().execute(() -> announce(state)));

        DonationAlertsIntegrate.registerEventListener(event -> {
            if (event.isTest() && DonationAlertsIntegrate.isSkipTestDonation())
                return;
            events.add(event);
            while (events.size() > MaxEvents)
                events.remove(0);
        });

        var category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Constants.ModId, "keys"));
        openKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping("key.daintegrate.open", InputConstants.Type.KEYSYM, InputConstants.KEY_EQUALS, category));

        if (configurationSource != null)
            uiContext = new MinecraftUiContext(configurationSource, client, tokens, events, eventProcessor, openKey);

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerCommands(dispatcher));

        ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
            eventProcessor.evalute();
            while (openKey.consumeClick())
                openMenu(null);
        });

        ClientPlayConnectionEvents.JOIN.register((handler, sender, minecraft) -> onJoin());

        autoConnect();
    }

    private void autoConnect() {
        var settings = configurationSource == null ? null : configurationSource.getCurrent();
        if (settings != null && settings.disableAutoConnect)
            return;
        var token = tokens.load();
        if (token != null)
            client.connect(token);
    }

    private void openMenu(DaMenu.Page page) {
        if (uiContext == null)
            return;
        var screen = new ImGuiScreen(uiContext);
        if (page != null)
            ImGuiScreen.menu().open(page);
        Minecraft.getInstance().setScreen(screen);
    }

    private void onJoin() {
        var settings = configurationSource == null ? null : configurationSource.getCurrent();
        if (settings != null && settings.disableWelcomeMessage)
            return;
        syncLanguage();
        if (!tokens.exists()) {
            send(prefix().append(I18n.t("chat.noToken")).append(openMenuLink()));
            return;
        }
        send(prefix().append(I18n.t("chat.welcome", keyName())).append(statusText(client.getState())));
    }

    /** Tells the player about connection changes, without repeating every reconnect attempt. */
    private void announce(ConnectionState state) {
        var previous = lastAnnounced;
        if (state == ConnectionState.Connecting || state == ConnectionState.Reconnecting)
            return;
        if (state == previous)
            return;
        lastAnnounced = state;
        syncLanguage();
        switch (state) {
            case Connected -> send(prefix().append(colored(I18n.t("chat.connected"), Green)));
            case Disconnected -> {
                if (previous == ConnectionState.Connected)
                    send(prefix().append(I18n.t("chat.disconnected")));
            }
            case Error -> send(prefix().append(colored(I18n.t("chat.error", String.valueOf(client.getLastError())), Red)));
            default -> {
            }
        }
    }

    private void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal("da")
                .executes(context -> {
                    // Commands run before the chat screen closes; open the menu on the next tick
                    Minecraft.getInstance().execute(() -> openMenu(null));
                    return 1;
                })
                .then(ClientCommands.literal("set").executes(this::setCommand))
                .then(ClientCommands.literal("connect").executes(this::connectCommand))
                .then(ClientCommands.literal("disconnect").executes(this::disconnectCommand))
                .then(ClientCommands.literal("status").executes(this::statusCommand))
        );
    }

    private int setCommand(CommandContext<FabricClientCommandSource> context) {
        syncLanguage();
        var token = TokenStore.parse(Minecraft.getInstance().keyboardHandler.getClipboard());
        if (token == null) {
            send(prefix().append(colored(I18n.t("chat.tokenInvalid"), Red)));
            return 0;
        }
        try {
            tokens.save(token);
        } catch (IOException e) {
            LOGGER.error("failed to save the token", e);
            send(prefix().append(colored(e.getMessage(), Red)));
            return 0;
        }
        send(prefix().append(I18n.t("chat.tokenSaved")));
        client.connect(token);
        return 1;
    }

    private int connectCommand(CommandContext<FabricClientCommandSource> context) {
        syncLanguage();
        var token = tokens.load();
        if (token == null) {
            send(prefix().append(I18n.t("chat.noToken")).append(openMenuLink()));
            return 0;
        }
        client.connect(token);
        return 1;
    }

    private int disconnectCommand(CommandContext<FabricClientCommandSource> context) {
        client.disconnect();
        return 1;
    }

    private int statusCommand(CommandContext<FabricClientCommandSource> context) {
        syncLanguage();
        send(prefix().append(I18n.t("chat.status", "")).append(statusText(client.getState())));
        return 1;
    }

    private MutableComponent statusText(ConnectionState state) {
        int color = switch (state) {
            case Connected -> Green;
            case Error -> Red;
            case Connecting, Reconnecting -> Orange;
            default -> 0xAAAAAA;
        };
        return colored(I18n.t("state." + state.name()), color);
    }

    private MutableComponent openMenuLink() {
        return Component.literal(I18n.t("chat.openMenu", keyName()))
                .withStyle(style -> style.withColor(TextColor.fromRgb(Link)).withUnderlined(true)
                        .withClickEvent(new ClickEvent.RunCommand("/da")));
    }

    private String keyName() {
        return openKey.getTranslatedKeyMessage().getString();
    }

    private static void syncLanguage() {
        I18n.setLanguage(Minecraft.getInstance().getLanguageManager().getSelected());
    }

    private static void send(Component message) {
        var player = Minecraft.getInstance().player;
        if (player != null)
            player.sendSystemMessage(message);
    }

    private static MutableComponent colored(String text, int rgb) {
        return Component.literal(text).withStyle(style -> style.withColor(TextColor.fromRgb(rgb)));
    }

    private static MutableComponent prefix() {
        return Component.literal("[")
                .append(colored("DA Integrate", Orange))
                .append("] ");
    }
}
