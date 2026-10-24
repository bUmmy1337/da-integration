package net.bummy1337.dontaionalerts;

public class ReadOnlyDonationAlertsEvent {
    private final DonationAlertsEvent event;
    private final long receivedAt;

    public ReadOnlyDonationAlertsEvent(DonationAlertsEvent event) {
        this.event = event;
        this.receivedAt = System.currentTimeMillis();
    }

    public long getReceivedAt() {
        return receivedAt;
    }

    public String getUserName() {
        return event.UserName;
    }

    public String getMessage() {
        return event.Message;
    }

    public AlertType getType() {
        return event.Type;
    }

    public String getCurrency() {
        return event.Currency;
    }

    public float getAmount() {
        return event.Amount;
    }

    public boolean isTest() {
        return event.IsTest;
    }
}
