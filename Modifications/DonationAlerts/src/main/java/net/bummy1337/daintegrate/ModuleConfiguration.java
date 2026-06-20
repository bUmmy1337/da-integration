package net.bummy1337.daintegrate;

import net.bummy1337.daintegrate.listeners.IListener;
import net.bummy1337.daintegrate.sensitives.ISensitive;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;

public class ModuleConfiguration implements IModuleConfiguration {
    private String modId;
    private String modUrl;

    public ModuleConfiguration(String modId, String modUrl) {
        this.modId = modId;
        this.modUrl = modUrl;
    }

    @Override
    public <T> IModuleConfiguration registerHandler(IHandler<T> handler) {
        DonationAlertsIntegrate.registerHandler(handler, modId, modUrl);
        return this;
    }

    @Override
    public <T> IModuleConfiguration registerSensitive(ISensitive<T> sensitive) {
        DonationAlertsIntegrate.registerSensitive(sensitive, modId, modUrl);
        return this;
    }

    @Override
    public IModuleConfiguration registerEventListener(IListener<ReadOnlyDonationAlertsEvent> event) {
        DonationAlertsIntegrate.registerEventListener(event);
        return this;
    }
}
