package net.bummy1337.testconsole;

import net.bummy1337.daintegrate.IHandler;
import net.bummy1337.daintegrate.handlers.MessageHandlerProperties;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;

public class MessageHandlerTest implements IHandler<MessageHandlerProperties> {
    @Override
    public void handle(ReadOnlyDonationAlertsEvent event, MessageHandlerProperties properties) {
    }

    @Override
    public String getImplementationId() {
        return MessageHandlerProperties.ImplementationId;
    }
}
