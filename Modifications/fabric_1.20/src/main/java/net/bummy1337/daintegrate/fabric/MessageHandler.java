package net.bummy1337.daintegrate.fabric;

import net.bummy1337.daintegrate.IHandler;
import net.bummy1337.daintegrate.ReplaceHelper;
import net.bummy1337.daintegrate.handlers.MessageHandlerProperties;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class MessageHandler implements IHandler<MessageHandlerProperties> {
    @Override
    public void handle(ReadOnlyDonationAlertsEvent event, MessageHandlerProperties properties) {
        var player = Minecraft.getInstance().player;
        if (player == null)
            return;
        player.sendSystemMessage(Component.literal(ReplaceHelper.replace(properties.message, event, player.getName().getString())));
    }

    @Override
    public String getImplementationId() {
        return MessageHandlerProperties.ImplementationId;
    }
}
