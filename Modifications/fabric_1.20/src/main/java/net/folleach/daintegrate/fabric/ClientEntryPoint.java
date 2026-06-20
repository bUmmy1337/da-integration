package net.folleach.daintegrate.fabric;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.PacketSender;
import net.folleach.daintegrate.Constants;
import net.folleach.daintegrate.DonationAlertsIntegrate;
import net.folleach.daintegrate.DonationAlertsIntegrateFactory;
import net.folleach.daintegrate.EventProcessor;
import net.folleach.daintegrate.configurations.sources.FileConfigurationSource;
import net.folleach.daintegrate.fabric.screen.MainScreen;
import net.folleach.daintegrate.listeners.DonationAlertsEventListener;
import net.folleach.dontaionalerts.DonationAlertsClient;
import net.folleach.dontaionalerts.ReadOnlyDonationAlertsEvent;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextColor;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ClientEntryPoint implements ClientModInitializer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Constants.ModId);
    private static FileConfigurationSource configurationSource = null;
    private DonationAlertsClient client;
    private KeyMapping openWindowKey;
    private List<ReadOnlyDonationAlertsEvent> donations = new ArrayList<>();

    @Override
    public void onInitializeClient() {
        configurationSource = null;

        DonationAlertsIntegrate.configure(Constants.ModId, Constants.ModUrl)
                .registerHandler(new MessageHandler())
                .registerHandler(new CommandHandler());

        try {
            configurationSource = DonationAlertsIntegrateFactory.create(
                    "donation-alerts-integrate",
                    "settings.yaml",
                    LOGGER::info);
        } catch (IOException e) {
            LOGGER.error("failed to initialize donation-alerts-integrate", e);
        }

        if (configurationSource != null) {
            configurationSource.addListener(settings -> {
                var player = Minecraft.getInstance().player;
                if (player == null)
                    return;
                if (settings.disableSettingsUpdateMessage)
                    return;
                player.sendSystemMessage(getPrefix().append("settings updated with ")
                        .append(String.valueOf(settings.triggers == null ? 0 : settings.triggers.length))
                        .append(" triggers")
                );
            });
        }

        var eventProcessor = new EventProcessor();
        var listener = new DonationAlertsEventListener(eventProcessor);
        try {
            client = new DonationAlertsClient(Constants.DonationAlertsEventServer, listener);
        } catch (java.net.URISyntaxException e) {
            throw new RuntimeException(e);
        }

        DonationAlertsIntegrate.registerEventListener(readOnly -> {
            if (readOnly.isTest() && DonationAlertsIntegrate.isSkipTestDonation())
                return;
            donations.add(readOnly);
            if (donations.size() > 100) {
                donations.remove(0);
            }
        });

        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> registerCommands(dispatcher));

        ClientTickEvents.END_CLIENT_TICK.register(t -> {
            eventProcessor.evalute();
        });

        var category = KeyMapping.Category.register(Identifier.fromNamespaceAndPath(Constants.ModId, "keys"));
        openWindowKey = KeyMappingHelper.registerKeyMapping(
                new KeyMapping(
                        "key.daintegrate.open",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_EQUAL,
                        category
                )
        );

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (openWindowKey.consumeClick()) {
                if (configurationSource != null) {
                    Minecraft.getInstance().setScreen(new MainScreen(configurationSource, this.client, donations));
                }
            }
        });

        ClientPlayConnectionEvents.JOIN.register(new ClientPlayConnectionEvents.Join() {
            @Override
            public void onPlayReady(ClientPacketListener handler, PacketSender sender, Minecraft client) {
                if (configurationSource != null) {
                    configurationSource.startListening();
                    var player = Minecraft.getInstance().player;
                    if (player == null)
                        return;
                    var current = configurationSource.getCurrent();
                    if (current == null || current.disableWelcomeMessage)
                        return;
                    sendWelcomeMessage(player);
                }
            }
        });
    }

    private void registerCommands(CommandDispatcher<FabricClientCommandSource> dispatcher) {
        dispatcher.register(ClientCommands.literal("da")
                .then(ClientCommands.literal("set")
                        .executes(this::setCommand)
                )
                .then(ClientCommands.literal("connect")
                        .executes(this::connectCommand)
                )
                .then(ClientCommands.literal("disconnect")
                        .executes(this::disconnectCommand)
                )
                .then(ClientCommands.literal("status")
                        .executes(this::statusCommand)
                )
        );
    }

    private int statusCommand(CommandContext<FabricClientCommandSource> context) {
        context.getSource().getPlayer().sendSystemMessage(getPrefix().append(client.getConnected() ? "Connected" : "Not connected"));
        return 0;
    }

    private int disconnectCommand(CommandContext<FabricClientCommandSource> context) {
        client.disconnect();
        return 0;
    }

    private int connectCommand(CommandContext<FabricClientCommandSource> context) {
        var home = System.getProperty("user.home");
        var token = "";
        try (BufferedReader br = new BufferedReader(new FileReader(new File(home, Constants.TokenFileName)))) {
            String line;
            while ((line = br.readLine()) != null && line.length() > 3) {
                token = line.trim();
            }
        } catch (FileNotFoundException e) {
            sendTokenNotFoundMessage(context.getSource().getPlayer());
            return 1;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (token.isEmpty()) {
            sendTokenNotFoundMessage(context.getSource().getPlayer());
            return 1;
        }
        var connected = client.connect(token);
        if (connected)
            context.getSource().getPlayer().sendSystemMessage(getPrefix().append("Successfully connected"));
        else
            context.getSource().getPlayer().sendSystemMessage(getPrefix().append("Failed to connect :(").withStyle(style -> style.withColor(TextColor.fromRgb(0xAA0000))));
        return 0;
    }

    private int setCommand(CommandContext<FabricClientCommandSource> context) {
        try {
            var clipboard = Minecraft.getInstance().keyboardHandler.getClipboard();
            if (clipboard == null || clipboard.trim().length() < 3) {
                context.getSource().getPlayer().sendSystemMessage(getPrefix().append("The copied text does not look like a token"));
                return 1;
            }
            clipboard = clipboard.trim();
            var home = System.getProperty("user.home");
            PrintWriter writer = new PrintWriter(new File(home, Constants.TokenFileName), StandardCharsets.UTF_8);
            writer.println(clipboard);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        context.getSource().getPlayer().sendSystemMessage(getPrefix().append("The token is set"));
        return 0;
    }

    private static void sendTokenNotFoundMessage(LocalPlayer player) {
        player.sendSystemMessage(getPrefix()
                .append("Token not found. ")
                .append(Component.literal("Learn more")
                        .withStyle(style -> style.withClickEvent(new ClickEvent.OpenUrl(URI.create(Constants.GuideToSetToken))))
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0x55FFFF)))
                )
        );
    }

    private static void sendWelcomeMessage(LocalPlayer player) {
        player.sendSystemMessage(getPrefix()
                .append("Settings stored in ")
                .append(Component.literal("donation-alerts-integrate/settings.yaml")
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0xAAAAAA)))
                )
                .append(Component.literal(". "))
                .append(Component.literal("Click to open")
                        .withStyle(style -> style.withClickEvent(new ClickEvent.OpenFile("donation-alerts-integrate/settings.yaml")))
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0x55FFFF)))
                )
        );
        player.sendSystemMessage(getPrefix()
                .append("Click ")
                .append(Component.literal("here")
                        .withStyle(style -> style.withClickEvent(new ClickEvent.OpenUrl(URI.create(Constants.GuideToConfiguration))))
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0x55FFFF)))
                )
                .append(" to open manual")
        );
    }

    private static MutableComponent getPrefix() {
        return Component.literal("[")
                .append(Component.literal("DA Integrate")
                        .withStyle(style -> style.withColor(TextColor.fromRgb(0xFFAA00)))
                )
                .append("] ");
    }
}
