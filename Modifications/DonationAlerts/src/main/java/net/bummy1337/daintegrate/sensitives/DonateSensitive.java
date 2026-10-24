package net.bummy1337.daintegrate.sensitives;

import net.bummy1337.dontaionalerts.AlertType;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;

public class DonateSensitive implements ISensitive<DonateSensitiveProperties> {
    @Override
    public boolean isActive(ReadOnlyDonationAlertsEvent event, DonateSensitiveProperties properties) {
        return event.getType() == AlertType.Donate
                && event.getAmount() >= properties.from
                && event.getAmount() <= properties.to
                && currencyMatches(event.getCurrency(), properties.currency);
    }

    private static boolean currencyMatches(String actual, String expected) {
        if (expected == null || expected.isBlank())
            return true;
        return actual != null && actual.trim().equalsIgnoreCase(expected.trim());
    }

    @Override
    public String getImplementationId() {
        return "donate";
    }
}
