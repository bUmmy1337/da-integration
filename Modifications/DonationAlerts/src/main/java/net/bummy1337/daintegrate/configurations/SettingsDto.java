package net.bummy1337.daintegrate.configurations;

public class SettingsDto {
    public TriggerDto[] triggers;
    public boolean disableSettingsUpdateMessage;
    public boolean disableWelcomeMessage;
    public boolean skipTestDonation;
    public boolean disableAutoConnect;
    public float uiScale;
    public boolean disableToasts;
    /** TopLeft, TopRight, BottomLeft or BottomRight; empty means TopRight. */
    public String toastCorner;
}
