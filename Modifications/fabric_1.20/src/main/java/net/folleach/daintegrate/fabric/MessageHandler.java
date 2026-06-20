package net.folleach.daintegrate.fabric;

import net.folleach.daintegrate.IHandler;
import net.folleach.daintegrate.ReplaceHelper;
import net.folleach.daintegrate.handlers.MessageHandlerProperties;
import net.folleach.dontaionalerts.ReadOnlyDonationAlertsEvent;
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
