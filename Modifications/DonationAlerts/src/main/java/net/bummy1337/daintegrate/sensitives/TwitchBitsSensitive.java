package net.bummy1337.daintegrate.sensitives;

import net.bummy1337.dontaionalerts.AlertType;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;

public class TwitchBitsSensitive implements ISensitive<TwitchBitsSensitiveProperties> {
    @Override
    public String getImplementationId() {
        return "twitch/bits";
    }

    @Override
    public boolean isActive(ReadOnlyDonationAlertsEvent event, TwitchBitsSensitiveProperties properties) {
        return event.getType() == AlertType.TwitchBits
                && event.getAmount() >= properties.from
                && event.getAmount() <= properties.to;
    }
}
