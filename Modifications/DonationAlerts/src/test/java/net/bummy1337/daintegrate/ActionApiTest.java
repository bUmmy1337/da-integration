package net.bummy1337.daintegrate;

import net.bummy1337.daintegrate.configurations.SettingsDto;
import net.bummy1337.daintegrate.configurations.YamlSettingsTransformer;
import net.bummy1337.daintegrate.configurations.sources.FileConfigurationSource;
import net.bummy1337.daintegrate.listeners.IListener;
import net.bummy1337.daintegrate.sensitives.AlwaysSensitive;
import net.bummy1337.dontaionalerts.AlertType;
import net.bummy1337.dontaionalerts.DonationAlertsEvent;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Goes through what an addon mod does: registers an action, gets it loaded from the settings file
 * and run for a donation, the way the game does it.
 */
public class ActionApiTest {
    public static class StrikeSettings {
        public String text = "default";
        public int count = 1;
        public float power = 1;
        public boolean loud;
    }

    private static final List<String> calls = new CopyOnWriteArrayList<>();
    private static final List<ReadOnlyDonationAlertsEvent> heard = new CopyOnWriteArrayList<>();

    @BeforeAll
    static void registerAddon() {
        DonationAlertsIntegrate.configure(Constants.ModId, Constants.ModUrl)
                .registerSensitive(new AlwaysSensitive());
        DonationAlertsIntegrate.configure("exampleaddon", "https://example.com")
                .registerAction("strike", "Strike", StrikeSettings.class, (event, settings) ->
                        calls.add("strike " + settings.count + " " + settings.power + " " + settings.loud + " "
                                + ReplaceHelper.replace(settings.text, event, "Steve")))
                .registerAction("broken", "Broken", None.class, (event, settings) -> {
                    throw new IllegalStateException("addon bug");
                })
                .registerEventListener(heard::add);
    }

    private static final String Settings = """
            triggers:
            - name: addon
              isActive: true
              sensitives:
              - properties:
                  type: daintegrate/sensitive/always
              handlers:
              - delay: 0
                properties:
                  type: exampleaddon/handler/broken
              - delay: 0
                properties:
                  type: exampleaddon/handler/strike
                  value:
                    text: '{username} sent {amount} {currency}'
                    count: 3
                    power: 2.5
                    loud: true
              - delay: 0
                properties:
                  type: removedmod/handler/explode
                  value:
                    radius: 4
              - delay: 20
                properties:
                  type: exampleaddon/handler/strike
                  value:
                    text: later
            """;

    @Test
    void addonActionRunsFromSettings() {
        calls.clear();
        var settings = new YamlSettingsTransformer().transform(Settings);
        Assertions.assertInstanceOf(StrikeSettings.class, settings.triggers[0].handlers[1].properties.value);

        DonationAlertsIntegrate.Instance.updateTriggers(settings.triggers);
        var processor = new EventProcessor();
        processor.onValue(event());

        // The broken action and the one from a removed mod do not stop the rest
        Assertions.assertEquals(List.of("strike 3 2.5 true Viewer sent 150.0 RUB"), calls);

        // The delayed one waits for 20 game ticks; untouched fields keep their defaults
        for (int tick = 0; tick < 19; tick++)
            processor.evalute();
        Assertions.assertEquals(1, calls.size());
        processor.evalute();
        Assertions.assertEquals("strike 1 1.0 false later", calls.get(1));
    }

    @Test
    void addonSettingsSurviveSaving() throws IOException {
        var dir = Files.createTempDirectory("da-api-test");
        var file = "settings.yaml";
        Files.writeString(dir.resolve(file), Settings);
        var loaded = new ArrayList<SettingsDto>();
        var listeners = new ArrayList<IListener<SettingsDto>>();
        listeners.add(loaded::add);
        var source = new FileConfigurationSource(dir.toString(), file, listeners, new YamlSettingsTransformer(), message -> {
        });
        source.reload();
        Assertions.assertEquals(1, loaded.size());

        source.save(loaded.get(0));
        var saved = Files.readString(dir.resolve(file));
        Assertions.assertFalse(saved.contains("!!"), saved);
        Assertions.assertTrue(saved.contains("removedmod/handler/explode"), saved);

        var again = new YamlSettingsTransformer().transform(saved);
        var strike = (StrikeSettings) again.triggers[0].handlers[1].properties.value;
        Assertions.assertEquals(3, strike.count);
        Assertions.assertEquals(2.5f, strike.power);
        Assertions.assertTrue(strike.loud);
        Assertions.assertEquals("removedmod/handler/explode", again.triggers[0].handlers[2].properties.type);
        source.stopListening();
    }

    @Test
    void addonActionsAreListedForTheMenu() {
        var strike = DonationAlertsIntegrate.getHandler("exampleaddon/handler/strike");
        Assertions.assertNotNull(strike);
        Assertions.assertEquals("Strike", strike.getTitle());
        var names = new ArrayList<String>();
        for (var property : strike.getProperties().getProperties())
            names.add(property.name + ":" + property.type.getSimpleName());
        Assertions.assertEquals(List.of("text:String", "count:int", "power:float", "loud:boolean"), names);
        Assertions.assertTrue(DonationAlertsIntegrate.getActions().contains(strike));
    }

    @Test
    void eventListenersHearEveryEvent() {
        heard.clear();
        DonationAlertsIntegrate.getEventListeners().forEachRemaining(listener -> listener.onValue(event()));
        Assertions.assertEquals(1, heard.size());
        Assertions.assertEquals("Viewer", heard.get(0).getUserName());
    }

    private static ReadOnlyDonationAlertsEvent event() {
        var event = new DonationAlertsEvent();
        event.UserName = "Viewer";
        event.Amount = 150;
        event.Currency = "RUB";
        event.Message = "hi";
        event.Type = AlertType.Donate;
        return new ReadOnlyDonationAlertsEvent(event);
    }
}
