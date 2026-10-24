package net.bummy1337.daintegrate.ui;

import net.bummy1337.daintegrate.configurations.HandlerPropertiesDto;
import net.bummy1337.daintegrate.configurations.PropertiesDto;
import net.bummy1337.daintegrate.configurations.SensitivePropertiesDto;
import net.bummy1337.daintegrate.configurations.TriggerDto;
import net.bummy1337.daintegrate.DonationAlertsIntegrate;
import net.bummy1337.daintegrate.handlers.CommandHandlerProperties;
import net.bummy1337.daintegrate.handlers.SoundHandlerProperties;
import net.bummy1337.daintegrate.sensitives.DonateSensitiveProperties;
import net.bummy1337.daintegrate.sensitives.SubscribeSensitiveProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;

public class TriggerModelTest {
    @Test
    void roundTripKeepsActionOrderAndDelays() {
        var model = new TriggerModel();
        model.name = "Night";
        model.kind = TriggerModel.Kind.Donate;
        model.from = 500;
        model.currency = "RUB";
        model.actions.add(TriggerModel.Action.command("/time set night", 0));
        model.actions.add(TriggerModel.Action.message("Hi {username}", 40));
        model.actions.add(TriggerModel.Action.command("summon zombie", 60));

        var dto = model.toDto();
        Assertions.assertEquals("daintegrate/sensitive/donate", dto.sensitives[0].properties.type);
        var donate = (DonateSensitiveProperties) dto.sensitives[0].properties.value;
        Assertions.assertEquals(500, donate.from);
        Assertions.assertEquals(TriggerModel.Unlimited, donate.to);
        Assertions.assertEquals("time set night", ((CommandHandlerProperties) dto.handlers[0].properties.value).command);
        Assertions.assertEquals(40, dto.handlers[1].delay);

        var back = TriggerModel.fromDto(dto);
        Assertions.assertEquals(3, back.actions.size());
        Assertions.assertEquals(TriggerModel.MessageType, back.actions.get(1).type);
        Assertions.assertEquals(60, back.actions.get(2).delayTicks);
        Assertions.assertEquals("RUB", back.currency);
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void readsLegacySubscribeTrigger() {
        var dto = new TriggerDto();
        dto.name = "sub";
        dto.isActive = true;
        var props = new PropertiesDto();
        props.type = "daintegrate/sensitive/subscribe";
        var value = new SubscribeSensitiveProperties();
        value.type = "TwitchSubscription";
        props.value = value;
        var sensitive = new SensitivePropertiesDto();
        sensitive.properties = props;
        dto.sensitives = new SensitivePropertiesDto[]{sensitive};
        dto.handlers = new HandlerPropertiesDto[0];

        var model = TriggerModel.fromDto(dto);
        Assertions.assertEquals(TriggerModel.Kind.Subscribe, model.kind);
        Assertions.assertEquals("TwitchSubscription", model.subscriptionType);
        Assertions.assertTrue(model.sameAs(model.copy()));
    }

    @Test
    void builtInActionsKeepTypedSettings() {
        var model = TriggerModel.create("Creeper");
        model.actions.clear();
        var sound = new TriggerModel.Action("daintegrate/handler/sound", 20);
        Assertions.assertEquals("minecraft:entity.player.levelup", sound.fields.get("sound"));
        sound.fields.put("volume", "0,5");
        model.actions.add(sound);

        var dto = model.toDto();
        var value = (SoundHandlerProperties) dto.handlers[0].properties.value;
        Assertions.assertEquals(0.5f, value.volume);
        Assertions.assertEquals(1f, value.pitch);

        var back = TriggerModel.fromDto(dto);
        Assertions.assertEquals("0.5", back.actions.get(0).fields.get("volume"));
        Assertions.assertTrue(back.sameAs(back.copy()));
    }

    @Test
    void changingTypeKeepsText() {
        var action = TriggerModel.Action.message("hello {username}", 0);
        action.changeType("daintegrate/handler/title");
        Assertions.assertEquals("hello {username}", action.fields.get("title"));
        Assertions.assertEquals("3", action.fields.get("seconds"));
    }

    @Test
    @SuppressWarnings({"rawtypes", "unchecked"})
    void keepsActionsOfMissingMods() {
        var dto = new TriggerDto();
        dto.name = "addon";
        dto.isActive = true;
        dto.sensitives = new SensitivePropertiesDto[0];
        var handler = new HandlerPropertiesDto();
        handler.delay = 10;
        handler.properties = new PropertiesDto();
        handler.properties.type = "othermod/handler/explode";
        var raw = new LinkedHashMap<String, Object>();
        raw.put("power", 4);
        handler.properties.value = raw;
        dto.handlers = new HandlerPropertiesDto[]{handler};

        var model = TriggerModel.fromDto(dto);
        Assertions.assertFalse(model.actions.get(0).isEditable());
        var saved = model.toDto();
        Assertions.assertEquals("othermod/handler/explode", saved.handlers[0].properties.type);
        Assertions.assertSame(raw, saved.handlers[0].properties.value);
        Assertions.assertEquals(10, saved.handlers[0].delay);
    }

    @Test
    void offersActionsOfOtherMods() {
        DonationAlertsIntegrate.configure("othermod", "")
                .registerAction("lightning", "Lightning", LightningProperties.class, (event, properties) -> {
                });
        Assertions.assertTrue(TriggerModel.availableTypes().contains("othermod/handler/lightning"));
        Assertions.assertEquals("Lightning", TriggerModel.titleOf("othermod/handler/lightning"));

        var action = new TriggerModel.Action("othermod/handler/lightning", 0);
        Assertions.assertTrue(action.isEditable());
        action.fields.put("count", "3");
        action.fields.put("loud", "true");
        var model = new TriggerModel();
        model.actions.add(action);
        var value = (LightningProperties) model.toDto().handlers[0].properties.value;
        Assertions.assertEquals(3, value.count);
        Assertions.assertTrue(value.loud);
    }

    public static class LightningProperties {
        public int count = 1;
        public boolean loud;
    }
}
