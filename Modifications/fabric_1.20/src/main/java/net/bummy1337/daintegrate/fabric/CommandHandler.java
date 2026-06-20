package net.bummy1337.daintegrate.fabric;

import net.bummy1337.daintegrate.IHandler;
import net.bummy1337.daintegrate.ReplaceHelper;
import net.bummy1337.daintegrate.handlers.CommandHandlerProperties;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;
import net.minecraft.client.Minecraft;

public class CommandHandler implements IHandler<CommandHandlerProperties> {
    @Override
    public void handle(ReadOnlyDonationAlertsEvent event, CommandHandlerProperties properties) {
        var player = Minecraft.getInstance().player;
        if (player == null)
            return;
        player.connection.sendCommand(ReplaceHelper.replace(properties.command, event, player.getName().getString()));
    }

    @Override
    public String getImplementationId() {
        return CommandHandlerProperties.ImplementationId;
    }
}
