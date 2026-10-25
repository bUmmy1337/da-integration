package net.bummy1337.daintegrate.fabric;

import net.bummy1337.daintegrate.IModuleConfiguration;
import net.bummy1337.daintegrate.ReplaceHelper;
import net.bummy1337.daintegrate.handlers.ActionBarHandlerProperties;
import net.bummy1337.daintegrate.handlers.ChatHandlerProperties;
import net.bummy1337.daintegrate.handlers.SoundHandlerProperties;
import net.bummy1337.daintegrate.handlers.TitleHandlerProperties;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;

/** Actions besides chat messages and commands that the mod itself offers. They run on the game thread. */
final class GameActions {
    /** The server drops longer chat messages. */
    private static final int MaxChatLength = 256;

    private GameActions() {
    }

    static void register(IModuleConfiguration configuration) {
        configuration
                .registerAction(TitleHandlerProperties.ImplementationId, "Title", TitleHandlerProperties.class, GameActions::title)
                .registerAction(ActionBarHandlerProperties.ImplementationId, "Action bar", ActionBarHandlerProperties.class, GameActions::actionBar)
                .registerAction(SoundHandlerProperties.ImplementationId, "Sound", SoundHandlerProperties.class, GameActions::sound)
                .registerAction(ChatHandlerProperties.ImplementationId, "Say in chat", ChatHandlerProperties.class, GameActions::chat);
    }

    private static void title(ReadOnlyDonationAlertsEvent event, TitleHandlerProperties properties) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null)
            return;
        int stay = Math.max(1, Math.round(properties.seconds * 20));
        minecraft.gui.hud.setTimes(10, stay, 20);
        minecraft.gui.hud.setSubtitle(Component.literal(replace(properties.subtitle, event)));
        minecraft.gui.hud.setTitle(Component.literal(replace(properties.title, event)));
    }

    private static void actionBar(ReadOnlyDonationAlertsEvent event, ActionBarHandlerProperties properties) {
        var minecraft = Minecraft.getInstance();
        if (minecraft.player == null)
            return;
        minecraft.gui.hud.setOverlayMessage(Component.literal(replace(properties.text, event)), false);
    }

    private static void sound(ReadOnlyDonationAlertsEvent event, SoundHandlerProperties properties) {
        if (properties.sound == null || properties.sound.isBlank())
            return;
        var id = Identifier.tryParse(properties.sound.trim());
        if (id == null)
            return;
        var sound = SoundEvent.createVariableRangeEvent(id);
        Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(sound, properties.pitch, properties.volume));
    }

    private static void chat(ReadOnlyDonationAlertsEvent event, ChatHandlerProperties properties) {
        var player = Minecraft.getInstance().player;
        if (player == null)
            return;
        var text = replace(properties.message, event).replace('\n', ' ').trim();
        if (text.isEmpty())
            return;
        if (text.length() > MaxChatLength)
            text = text.substring(0, MaxChatLength);
        if (text.startsWith("/"))
            PlayerCommands.run(text.substring(1));
        else
            player.connection.sendChat(text);
    }

    private static String replace(String pattern, ReadOnlyDonationAlertsEvent event) {
        var player = Minecraft.getInstance().player;
        return ReplaceHelper.replace(pattern, event, player == null ? "" : player.getName().getString());
    }
}
