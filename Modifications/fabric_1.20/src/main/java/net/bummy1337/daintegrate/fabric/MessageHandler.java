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
        Minecraft mc = Minecraft.getInstance();
        mc.execute(() -> {
            var player = mc.player;
            if (player == null) {
                return;
            }
            String text = ReplaceHelper.replace(properties.message, event, player.getName().getString());
            player.sendSystemMessage(Component.literal(text));
        });
    }

    @Override
    public String getImplementationId() {
        return MessageHandlerProperties.ImplementationId;
    }
}
