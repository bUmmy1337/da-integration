package net.bummy1337.daintegrate.sensitives;

import net.bummy1337.daintegrate.None;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;

public class AlwaysSensitive implements ISensitive<None> {
    @Override
    public String getImplementationId() {
        return "always";
    }

    @Override
    public boolean isActive(ReadOnlyDonationAlertsEvent event, None properties) {
        return true;
    }
}
