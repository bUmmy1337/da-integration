package net.bummy1337.daintegrate.ui.preview;

import net.bummy1337.daintegrate.Constants;
import net.bummy1337.daintegrate.TokenStore;
import net.bummy1337.daintegrate.configurations.SettingsDto;
import net.bummy1337.daintegrate.configurations.TriggerDto;
import net.bummy1337.daintegrate.ui.TriggerModel;
import net.bummy1337.daintegrate.ui.UiContext;
import net.bummy1337.dontaionalerts.AlertType;
import net.bummy1337.dontaionalerts.ConnectionState;
import net.bummy1337.dontaionalerts.DonationAlertsClient;
import net.bummy1337.dontaionalerts.DonationAlertsEvent;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/** Sample data for the preview window; nothing here talks to Donation Alerts. */
class PreviewContext implements UiContext {
    private final DonationAlertsClient client;
    private final TokenStore tokens;
    private final List<ReadOnlyDonationAlertsEvent> events = new ArrayList<>();
    private final Runnable close;
    private SettingsDto settings;
    private String clipboard = "https://www.donationalerts.com/widget/alerts?group_id=1&token=PreviewToken12345";

    PreviewContext(boolean withToken, String state, Runnable close) throws Exception {
        this.close = close;
        client = new DonationAlertsClient(Constants.DonationAlertsEventServer, event -> {
        });
        forceState(ConnectionState.valueOf(state));

        var tokenFile = File.createTempFile("da-preview-token", ".txt");
        tokenFile.deleteOnExit();
        tokens = new TokenStore(tokenFile);
        if (withToken)
            tokens.save("PreviewToken12345");
        else
            tokens.delete();

        settings = new SettingsDto();
        var thanks = TriggerModel.create("Спасибо за донат");
        thanks.actions.clear();
        thanks.actions.add(TriggerModel.Action.message("{username}, спасибо за {amount} {currency}! {message}", 0));
        var night = TriggerModel.create("Ночь за 500+");
        night.from = 500;
        night.currency = "RUB";
        night.actions.clear();
        night.actions.add(TriggerModel.Action.command("time set night", 0));
        night.actions.add(TriggerModel.Action.command("summon zombie ~ ~ ~", 60));
        var creeper = TriggerModel.create("Крипер за 1000");
        creeper.from = 1000;
        creeper.to = 4999;
        creeper.currency = "RUB";
        creeper.actions.clear();
        var title = new TriggerModel.Action("daintegrate/handler/title", 0);
        title.fields.put("title", "{username}");
        title.fields.put("subtitle", "призывает крипера!");
        creeper.actions.add(title);
        creeper.actions.add(new TriggerModel.Action("daintegrate/handler/sound", 0));
        creeper.actions.add(TriggerModel.Action.command("summon creeper ~ ~ ~2", 40));
        var sub = TriggerModel.create("Подписка YouTube");
        sub.kind = TriggerModel.Kind.Subscribe;
        sub.actions.clear();
        sub.actions.add(TriggerModel.Action.command("give {playername} diamond 1", 0));
        var points = TriggerModel.create("Баллы: прыжок");
        points.kind = TriggerModel.Kind.TwitchPoints;
        points.active = false;
        settings.triggers = new TriggerDto[]{thanks.toDto(), night.toDto(), creeper.toDto(), sub.toDto(), points.toDto()};

        if (!"Disconnected".equals(state)) {
            event("Алексей", 150, "RUB", "Привет! Отличный стрим", AlertType.Donate, 600_000, false);
            event("kotik_99", 1000, "RUB", "Давай крипера!", AlertType.Donate, 420_000, false);
            event("streamfan", 5, "USD", "gl hf", AlertType.Donate, 300_000, false);
            event("Мария", 0, null, null, AlertType.YouTubeSubscription, 180_000, false);
            event("Тест", 100, "RUB", "Тестовое оповещение", AlertType.Donate, 60_000, true);
            event("nightowl", 500, "RUB", "Включай ночь", AlertType.Donate, 12_000, false);
        }
    }

    private void forceState(ConnectionState state) throws Exception {
        var field = DonationAlertsClient.class.getDeclaredField("state");
        field.setAccessible(true);
        field.set(client, state);
        if (state == ConnectionState.Connected) {
            var since = DonationAlertsClient.class.getDeclaredField("lastEventAt");
            since.setAccessible(true);
            since.set(client, System.currentTimeMillis() - 12_000);
        }
        if (state == ConnectionState.Error) {
            var error = DonationAlertsClient.class.getDeclaredField("lastError");
            error.setAccessible(true);
            error.set(client, "timeout");
        }
    }

    private void event(String user, float amount, String currency, String message, AlertType type, long ago, boolean test) throws Exception {
        var raw = new DonationAlertsEvent();
        raw.UserName = user;
        raw.Amount = amount;
        raw.Currency = currency;
        raw.Message = message;
        raw.Type = type;
        raw.IsTest = test;
        var event = new ReadOnlyDonationAlertsEvent(raw);
        var field = ReadOnlyDonationAlertsEvent.class.getDeclaredField("receivedAt");
        field.setAccessible(true);
        field.set(event, System.currentTimeMillis() - ago);
        events.add(event);
    }

    @Override
    public SettingsDto getSettings() {
        return settings;
    }

    @Override
    public void saveSettings(SettingsDto settings) {
        this.settings = settings;
    }

    @Override
    public void reloadSettings() {
    }

    @Override
    public File getSettingsFile() {
        return new File(System.getProperty("user.home"), "donation-alerts-integrate/settings.yaml");
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
        System.out.println("simulate " + trigger.name + " with " + event.getAmount() + " " + event.getCurrency());
    }

    @Override
    public String getClipboard() {
        return clipboard;
    }

    @Override
    public void setClipboard(String text) {
        clipboard = text;
    }

    @Override
    public void openUrl(String url) {
        System.out.println("open " + url);
    }

    @Override
    public void openFile(File file) {
        System.out.println("open " + file);
    }

    @Override
    public String getOpenKeyName() {
        return "=";
    }

    @Override
    public void close() {
        close.run();
    }
}
