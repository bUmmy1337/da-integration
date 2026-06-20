package net.bummy1337.daintegrate;

import net.bummy1337.daintegrate.listeners.IListener;
import net.bummy1337.daintegrate.sensitives.ISensitive;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;

public interface IModuleConfiguration {
    <T> IModuleConfiguration registerHandler(IHandler<T> handler);
    <T> IModuleConfiguration registerSensitive(ISensitive<T> sensitive);
    IModuleConfiguration registerEventListener(IListener<ReadOnlyDonationAlertsEvent> event);
}
