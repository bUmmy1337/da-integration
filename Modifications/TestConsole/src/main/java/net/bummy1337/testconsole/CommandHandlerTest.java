package net.bummy1337.testconsole;

import net.bummy1337.daintegrate.IHandler;
import net.bummy1337.daintegrate.handlers.CommandHandlerProperties;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;

public class CommandHandlerTest implements IHandler<CommandHandlerProperties> {
    @Override
    public void handle(ReadOnlyDonationAlertsEvent event, CommandHandlerProperties properties) {
    }

    @Override
    public String getImplementationId() {
        return CommandHandlerProperties.ImplementationId;
    }
}
