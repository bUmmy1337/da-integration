package net.bummy1337.daintegrate.sensitives;

import net.bummy1337.daintegrate.IImplementationId;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;

public interface ISensitive<T> extends IImplementationId {
    boolean isActive(ReadOnlyDonationAlertsEvent event, T properties);
}
