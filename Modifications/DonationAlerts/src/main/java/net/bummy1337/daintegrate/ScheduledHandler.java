package net.bummy1337.daintegrate;

import net.bummy1337.daintegrate.configurations.HandlerPropertiesDto;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;

public class ScheduledHandler {
    public ReadOnlyDonationAlertsEvent event;
    public HandlerPropertiesDto handler;

    public ScheduledHandler(ReadOnlyDonationAlertsEvent event, HandlerPropertiesDto handler) {
        this.event = event;
        this.handler = handler;
    }
}
