package net.bummy1337.daintegrate.ui;

import net.bummy1337.daintegrate.Constants;
import net.bummy1337.daintegrate.configurations.HandlerPropertiesDto;
import net.bummy1337.daintegrate.configurations.PropertiesDto;
import net.bummy1337.daintegrate.configurations.SensitivePropertiesDto;
import net.bummy1337.daintegrate.configurations.TriggerDto;
import net.bummy1337.daintegrate.handlers.CommandHandlerProperties;
import net.bummy1337.daintegrate.handlers.MessageHandlerProperties;
import net.bummy1337.daintegrate.sensitives.DonateSensitiveProperties;
import net.bummy1337.daintegrate.sensitives.SubscribeSensitiveProperties;
import net.bummy1337.daintegrate.sensitives.TwitchBitsSensitiveProperties;
import net.bummy1337.daintegrate.sensitives.TwitchPointsSensitiveProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Editable, UI-friendly view of a {@link TriggerDto}: one condition and an ordered list of actions.
 */
public class TriggerModel {
    public enum Kind { Any, Donate, Subscribe, TwitchBits, TwitchPoints }

    public enum ActionType { Message, Command }

    public static final String[] SubscriptionTypes = {
            "YouTubeSubscription",
            "TwitchSubscription",
            "TwitchPrimeSubscription",
            "TwitchGiftSubscription",
            "TwitchFreeFollow"
    };

    public static final String[] Currencies = { "", "RUB", "USD", "EUR", "UAH", "KZT", "BYN", "BRL", "TRY", "PLN" };

    public static final float Unlimited = 999999;

    public static class Action {
        public ActionType type;
        public String text;
        /** Delay in game ticks, as stored in the settings file. */
        public int delayTicks;

        public Action(ActionType type, String text, int delayTicks) {
            this.type = type;
            this.text = text;
            this.delayTicks = delayTicks;
        }

        public Action copy() {
            return new Action(type, text, delayTicks);
        }
    }

    public String name = "";
    public String description;
    public boolean active = true;
    public Kind kind = Kind.Donate;
    public float from = 0;
    public float to = Unlimited;
    public String currency = "";
    public String subscriptionType = SubscriptionTypes[0];
    public final List<Action> actions = new ArrayList<>();

    public static TriggerModel create(String name) {
        var model = new TriggerModel();
        model.name = name;
        model.actions.add(new Action(ActionType.Message, "{username}: {amount} {currency}! {message}", 0));
        return model;
    }

    public TriggerModel copy() {
        var copy = new TriggerModel();
        copy.name = name;
        copy.description = description;
        copy.active = active;
        copy.kind = kind;
        copy.from = from;
        copy.to = to;
        copy.currency = currency;
        copy.subscriptionType = subscriptionType;
        for (var action : actions)
            copy.actions.add(action.copy());
        return copy;
    }

    public boolean sameAs(TriggerModel other) {
        if (other == null || actions.size() != other.actions.size())
            return false;
        for (int i = 0; i < actions.size(); i++) {
            var a = actions.get(i);
            var b = other.actions.get(i);
            if (a.type != b.type || a.delayTicks != b.delayTicks || !eq(a.text, b.text))
                return false;
        }
        return eq(name, other.name) && eq(description, other.description) && active == other.active
                && kind == other.kind && from == other.from && to == other.to
                && eq(currency, other.currency) && eq(subscriptionType, other.subscriptionType);
    }

    public boolean matches(String query) {
        if (query == null || query.isBlank())
            return true;
        var q = query.toLowerCase();
        if (name != null && name.toLowerCase().contains(q))
            return true;
        for (var action : actions)
            if (action.text != null && action.text.toLowerCase().contains(q))
                return true;
        return false;
    }

    public static TriggerModel fromDto(TriggerDto dto) {
        var model = new TriggerModel();
        model.name = dto.name == null ? "" : dto.name;
        model.description = dto.description;
        model.active = dto.isActive;
        model.kind = Kind.Any;
        if (dto.sensitives != null) {
            for (var sensitive : dto.sensitives) {
                if (sensitive == null || sensitive.properties == null || sensitive.properties.type == null)
                    continue;
                if (readSensitive(model, sensitive.properties))
                    break;
            }
        }
        if (dto.handlers != null) {
            for (var handler : dto.handlers) {
                if (handler == null || handler.properties == null || handler.properties.type == null)
                    continue;
                var type = shortType(handler.properties.type);
                var value = handler.properties.value;
                if ("message".equals(type))
                    model.actions.add(new Action(ActionType.Message, textOf(value, "message"), handler.delay));
                else if ("command".equals(type))
                    model.actions.add(new Action(ActionType.Command, textOf(value, "command"), handler.delay));
            }
        }
        return model;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public TriggerDto toDto() {
        var dto = new TriggerDto();
        dto.name = name;
        dto.description = description;
        dto.isActive = active;

        var properties = new PropertiesDto();
        switch (kind) {
            case Any -> properties.type = Constants.ModId + "/sensitive/always";
            case Donate -> {
                properties.type = Constants.ModId + "/sensitive/donate";
                var value = new DonateSensitiveProperties();
                value.from = from;
                value.to = to;
                value.currency = currency == null ? "" : currency;
                properties.value = value;
            }
            case Subscribe -> {
                properties.type = Constants.ModId + "/sensitive/subscribe";
                var value = new SubscribeSensitiveProperties();
                value.type = subscriptionType;
                properties.value = value;
            }
            case TwitchBits -> {
                properties.type = Constants.ModId + "/sensitive/twitch/bits";
                var value = new TwitchBitsSensitiveProperties();
                value.from = from;
                value.to = to;
                properties.value = value;
            }
            case TwitchPoints -> {
                properties.type = Constants.ModId + "/sensitive/twitch/points";
                var value = new TwitchPointsSensitiveProperties();
                value.from = from;
                value.to = to;
                properties.value = value;
            }
        }
        var sensitive = new SensitivePropertiesDto();
        sensitive.properties = properties;
        dto.sensitives = new SensitivePropertiesDto[]{sensitive};

        var handlers = new ArrayList<HandlerPropertiesDto>();
        for (var action : actions) {
            var handler = new HandlerPropertiesDto();
            handler.delay = Math.max(0, action.delayTicks);
            handler.properties = new PropertiesDto();
            if (action.type == ActionType.Message) {
                var value = new MessageHandlerProperties();
                value.message = action.text == null ? "" : action.text;
                handler.properties.type = Constants.ModId + "/handler/message";
                handler.properties.value = value;
            } else {
                var value = new CommandHandlerProperties();
                var command = action.text == null ? "" : action.text.trim();
                value.command = command.startsWith("/") ? command.substring(1) : command;
                handler.properties.type = Constants.ModId + "/handler/command";
                handler.properties.value = value;
            }
            handlers.add(handler);
        }
        dto.handlers = handlers.toArray(new HandlerPropertiesDto[0]);
        return dto;
    }

    private static boolean readSensitive(TriggerModel model, PropertiesDto<?> properties) {
        var value = properties.value;
        switch (shortType(properties.type)) {
            case "always" -> {
                model.kind = Kind.Any;
                return true;
            }
            case "donate" -> {
                model.kind = Kind.Donate;
                if (value instanceof DonateSensitiveProperties p) {
                    model.from = p.from;
                    model.to = p.to;
                    model.currency = p.currency == null ? "" : p.currency.trim();
                }
                return true;
            }
            case "subscribe" -> {
                model.kind = Kind.Subscribe;
                if (value instanceof SubscribeSensitiveProperties p && p.type != null)
                    model.subscriptionType = p.type;
                return true;
            }
            case "bits" -> {
                model.kind = Kind.TwitchBits;
                if (value instanceof TwitchBitsSensitiveProperties p) {
                    model.from = p.from;
                    model.to = p.to;
                }
                return true;
            }
            case "points" -> {
                model.kind = Kind.TwitchPoints;
                if (value instanceof TwitchPointsSensitiveProperties p) {
                    model.from = p.from;
                    model.to = p.to;
                }
                return true;
            }
        }
        return false;
    }

    private static String textOf(Object value, String field) {
        if (value instanceof MessageHandlerProperties p)
            return p.message;
        if (value instanceof CommandHandlerProperties p)
            return p.command;
        if (value instanceof Map<?, ?> map && map.get(field) != null)
            return String.valueOf(map.get(field));
        if (value instanceof String s)
            return s;
        return "";
    }

    private static String shortType(String type) {
        return type.contains("/") ? type.substring(type.lastIndexOf('/') + 1) : type;
    }

    private static boolean eq(String a, String b) {
        return (a == null ? "" : a).equals(b == null ? "" : b);
    }
}
