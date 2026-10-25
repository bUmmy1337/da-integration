package net.bummy1337.daintegrate;

import net.bummy1337.daintegrate.configurations.HandlerPropertiesDto;
import net.bummy1337.daintegrate.configurations.SensitivePropertiesDto;
import net.bummy1337.daintegrate.configurations.TriggerDto;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public class FileConfigurationSourceTest {
    @Test
    void triggersThatFireAreThoseSavedFromTheMenu() throws IOException, InterruptedException {
        var directory = Files.createTempDirectory("daintegrate");
        var source = DonationAlertsIntegrateFactory.create(directory.toString(), "settings.yaml", message -> {});
        source.startListening();
        try {
            // The watcher reads the file while it is being saved; it must never leave older triggers running
            for (int i = 0; i < 300; i++) {
                var settings = source.getCurrent();
                var count = 1 + i % 6;
                settings.triggers = new TriggerDto[count];
                for (int k = 0; k < count; k++)
                    settings.triggers[k] = trigger("trigger " + i + "." + k + " " + "x".repeat(i % 3 == 0 ? 3000 : 1));
                source.save(settings);
                Thread.sleep(30);

                var expected = Arrays.stream(settings.triggers).map(t -> t.name).toList();
                var menu = Arrays.stream(source.getCurrent().triggers).map(t -> t.name).toList();
                var firing = DonationAlertsIntegrate.getTriggers().map(t -> t.name).toList();
                Assertions.assertEquals(expected, menu, "menu after save " + i);
                Assertions.assertEquals(expected, firing, "triggers after save " + i);
            }
        } finally {
            source.stopListening();
        }
    }

    private static TriggerDto trigger(String name) {
        var trigger = new TriggerDto();
        trigger.name = name;
        trigger.isActive = true;
        trigger.sensitives = new SensitivePropertiesDto[0];
        trigger.handlers = new HandlerPropertiesDto[0];
        return trigger;
    }
}
