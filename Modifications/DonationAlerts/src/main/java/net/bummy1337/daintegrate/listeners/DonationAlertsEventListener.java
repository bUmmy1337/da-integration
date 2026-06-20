package net.bummy1337.daintegrate.listeners;

import net.bummy1337.daintegrate.DonationAlertsIntegrate;
import net.bummy1337.dontaionalerts.DonationAlertsEvent;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;

public class DonationAlertsEventListener implements IListener<DonationAlertsEvent> {
    private final IListener<ReadOnlyDonationAlertsEvent> processor;
    private int lastId = -1;
    private long lastTime;

    public DonationAlertsEventListener(IListener<ReadOnlyDonationAlertsEvent> processor) {
        this.processor = processor;
    }

    @Override
    public void onValue(DonationAlertsEvent value) {
        var now = System.currentTimeMillis();
        if (value.ID == lastId && now - lastTime < 5000)
            return;
        lastId = value.ID;
        lastTime = now;

        var readOnly = new ReadOnlyDonationAlertsEvent(value);
        DonationAlertsIntegrate.getEventListeners().forEachRemaining(listener -> {
            listener.onValue(readOnly);
        });
        processor.onValue(readOnly);
    }
}
