package net.bummy1337.daintegrate.ui;

import net.bummy1337.daintegrate.configurations.HandlerPropertiesDto;
import net.bummy1337.daintegrate.configurations.PropertiesDto;
import net.bummy1337.daintegrate.configurations.SensitivePropertiesDto;
import net.bummy1337.daintegrate.configurations.TriggerDto;
import net.bummy1337.daintegrate.handlers.CommandHandlerProperties;
import net.bummy1337.daintegrate.sensitives.DonateSensitiveProperties;
import net.bummy1337.daintegrate.sensitives.SubscribeSensitiveProperties;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TriggerModelTest {
    @Test
    void roundTripKeepsActionOrderAndDelays() {
        var model = new TriggerModel();
        model.name = "Night";
        model.kind = TriggerModel.Kind.Donate;
        model.from = 500;
        model.currency = "RUB";
        model.actions.add(new TriggerModel.Action(TriggerModel.ActionType.Command, "/time set night", 0));
        model.actions.add(new TriggerModel.Action(TriggerModel.ActionType.Message, "Hi {username}", 40));
        model.actions.add(new TriggerModel.Action(TriggerModel.ActionType.Command, "summon zombie", 60));

        var dto = model.toDto();
        Assertions.assertEquals("daintegrate/sensitive/donate", dto.sensitives[0].properties.type);
        var donate = (DonateSensitiveProperties) dto.sensitives[0].properties.value;
        Assertions.assertEquals(500, donate.from);
        Assertions.assertEquals(TriggerModel.Unlimited, donate.to);
        Assertions.assertEquals("time set night", ((CommandHandlerProperties) dto.handlers[0].properties.value).command);
        Assertions.assertEquals(40, dto.handlers[1].delay);

        var back = TriggerModel.fromDto(dto);
        Assertions.assertEquals(3, back.actions.size());
        Assertions.assertEquals(TriggerModel.ActionType.Message, back.actions.get(1).type);
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
}
