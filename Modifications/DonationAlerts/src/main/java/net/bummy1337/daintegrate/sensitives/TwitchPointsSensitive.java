package net.bummy1337.daintegrate.sensitives;

import net.bummy1337.dontaionalerts.AlertType;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;

public class TwitchPointsSensitive implements ISensitive<TwitchPointsSensitiveProperties> {
    @Override
    public String getImplementationId() {
        return "twitch/points";
    }

    @Override
    public boolean isActive(ReadOnlyDonationAlertsEvent event, TwitchPointsSensitiveProperties properties) {
        return event.getType() == AlertType.TwitchPoints
                && event.getAmount() >= properties.from
                && event.getAmount() <= properties.to;
    }
}
