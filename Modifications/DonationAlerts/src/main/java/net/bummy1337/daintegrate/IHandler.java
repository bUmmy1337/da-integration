package net.bummy1337.daintegrate;

import net.bummy1337.dontaionalerts.DonationAlertsEvent;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;

public interface IHandler<T> extends IImplementationId {
    void handle(ReadOnlyDonationAlertsEvent event, T properties);
}
