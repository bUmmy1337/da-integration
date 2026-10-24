package net.bummy1337.daintegrate.ui;

import net.bummy1337.daintegrate.TokenStore;
import net.bummy1337.daintegrate.configurations.SettingsDto;
import net.bummy1337.daintegrate.configurations.TriggerDto;
import net.bummy1337.dontaionalerts.DonationAlertsClient;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Everything the menu needs from the game, kept free of Minecraft classes so the
 * menu can also run in the standalone preview.
 */
public interface UiContext {
    SettingsDto getSettings();

    void saveSettings(SettingsDto settings) throws IOException;

    void reloadSettings();

    File getSettingsFile();

    DonationAlertsClient getClient();

    TokenStore getTokenStore();

    /** Newest last. */
    List<ReadOnlyDonationAlertsEvent> getEvents();

    /** Runs the actions of one trigger for a sample event, without adding it to the history. */
    void simulate(ReadOnlyDonationAlertsEvent event, TriggerDto trigger);

    String getClipboard();

    void setClipboard(String text);

    void openUrl(String url);

    void openFile(File file);

    String getOpenKeyName();

    void close();
}
