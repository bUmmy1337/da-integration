package net.bummy1337.dontaionalerts;

public class ReadOnlyDonationAlertsEvent {
    private final DonationAlertsEvent event;

    public ReadOnlyDonationAlertsEvent(DonationAlertsEvent event) {
        this.event = event;
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
