package net.bummy1337.daintegrate.fabric;

import net.bummy1337.daintegrate.EventProcessor;
import net.bummy1337.daintegrate.TokenStore;
import net.bummy1337.daintegrate.configurations.SettingsDto;
import net.bummy1337.daintegrate.configurations.TriggerDto;
import net.bummy1337.daintegrate.configurations.sources.FileConfigurationSource;
import net.bummy1337.daintegrate.ui.UiContext;
import net.bummy1337.dontaionalerts.DonationAlertsClient;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;
import com.mojang.blaze3d.Blaze3D;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.List;

/** Connects the menu to the running game. */
class MinecraftUiContext implements UiContext {
    private final FileConfigurationSource configuration;
    private final DonationAlertsClient client;
    private final TokenStore tokens;
    private final List<ReadOnlyDonationAlertsEvent> events;
    private final EventProcessor processor;
    private final KeyMapping openKey;

    MinecraftUiContext(FileConfigurationSource configuration, DonationAlertsClient client, TokenStore tokens,
                       List<ReadOnlyDonationAlertsEvent> events, EventProcessor processor, KeyMapping openKey) {
        this.configuration = configuration;
        this.client = client;
        this.tokens = tokens;
        this.events = events;
        this.processor = processor;
        this.openKey = openKey;
    }

    @Override
    public SettingsDto getSettings() {
        var current = configuration.getCurrent();
        return current != null ? current : new SettingsDto();
    }

    @Override
    public void saveSettings(SettingsDto settings) throws IOException {
        configuration.save(settings);
    }

    @Override
    public void reloadSettings() {
        configuration.reload();
    }

    @Override
    public File getSettingsFile() {
        return configuration.getFile().getAbsoluteFile();
    }

    @Override
    public DonationAlertsClient getClient() {
        return client;
    }

    @Override
    public TokenStore getTokenStore() {
        return tokens;
    }

    @Override
    public List<ReadOnlyDonationAlertsEvent> getEvents() {
        return events;
    }

    @Override
    public void simulate(ReadOnlyDonationAlertsEvent event, TriggerDto trigger) {
        Minecraft.getInstance().execute(() -> processor.run(event, trigger));
    }

    @Override
    public String getClipboard() {
        return Minecraft.getInstance().keyboardHandler.getClipboard();
    }

    @Override
    public void setClipboard(String text) {
        Minecraft.getInstance().keyboardHandler.setClipboard(text);
    }

    @Override
    public void openUrl(String url) {
        Blaze3D.openUri(URI.create(url));
    }

    @Override
    public void openFile(File file) {
        Blaze3D.openPath(file.toPath());
    }

    @Override
    public String getOpenKeyName() {
        return openKey.getTranslatedKeyMessage().getString();
    }

    @Override
    public void close() {
        Minecraft.getInstance().execute(() -> Minecraft.getInstance().setScreenAndShow(null));
    }
}
