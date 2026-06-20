package net.bummy1337.daintegrate.sensitives;

import net.bummy1337.dontaionalerts.AlertType;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;

public class DonateSensitive implements ISensitive<DonateSensitiveProperties> {
    @Override
    public boolean isActive(ReadOnlyDonationAlertsEvent event, DonateSensitiveProperties properties) {
        return event.getType() == AlertType.Donate
                && event.getAmount() >= properties.from
                && event.getAmount() <= properties.to
                && event.getCurrency().trim().equalsIgnoreCase(properties.currency.trim());
    }

    @Override
    public String getImplementationId() {
        return "donate";
    }
}
