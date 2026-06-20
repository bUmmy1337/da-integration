package net.bummy1337.daintegrate;

import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;

public class ReplaceHelper {
    public static final String TagDonationMessage      = "{message}";
    public static final String TagDonationAmount       = "{amount}";
    public static final String TagDonationCurrency     = "{currency}";
    public static final String TagDonationUserName     = "{username}";
    public static final String TagMinecraftPlayerName  = "{playername}";

    public static String replace(String pattern, ReadOnlyDonationAlertsEvent event, String playerName) {
        if (pattern == null)
            return "";
        pattern = pattern.replace(TagDonationMessage, event.getMessage() == null ? "" : event.getMessage());
        pattern = pattern.replace(TagDonationAmount, String.valueOf(event.getAmount()));
        pattern = pattern.replace(TagDonationCurrency, String.valueOf(event.getCurrency()));
        pattern = pattern.replace(TagDonationUserName, String.valueOf(event.getUserName()));
        pattern = pattern.replace(TagMinecraftPlayerName, playerName);
        return pattern;
    }
}
