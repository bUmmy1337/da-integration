package net.bummy1337.daintegrate.ui;

import net.bummy1337.daintegrate.Constants;
import net.bummy1337.daintegrate.configurations.HandlerPropertiesDto;
import net.bummy1337.daintegrate.configurations.PropertiesDto;
import net.bummy1337.daintegrate.configurations.SensitivePropertiesDto;
import net.bummy1337.daintegrate.configurations.TriggerDto;
import net.bummy1337.daintegrate.DonationAlertsIntegrate;
import net.bummy1337.daintegrate.EntityId;
import net.bummy1337.daintegrate.Property;
import net.bummy1337.daintegrate.descriptors.PropertiesDescriptor;
import net.bummy1337.daintegrate.handlers.ActionBarHandlerProperties;
import net.bummy1337.daintegrate.handlers.ChatHandlerProperties;
import net.bummy1337.daintegrate.handlers.CommandHandlerProperties;
import net.bummy1337.daintegrate.handlers.MessageHandlerProperties;
import net.bummy1337.daintegrate.handlers.SoundHandlerProperties;
import net.bummy1337.daintegrate.handlers.TitleHandlerProperties;
import net.bummy1337.daintegrate.sensitives.DonateSensitiveProperties;
import net.bummy1337.daintegrate.sensitives.SubscribeSensitiveProperties;
import net.bummy1337.daintegrate.sensitives.TwitchBitsSensitiveProperties;
import net.bummy1337.daintegrate.sensitives.TwitchPointsSensitiveProperties;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Editable, UI-friendly view of a {@link TriggerDto}: one condition and an ordered list of actions.
 */
public class TriggerModel {
    public enum Kind { Any, Donate, Subscribe, TwitchBits, TwitchPoints }

    public static final String[] SubscriptionTypes = {
            "YouTubeSubscription",
            "TwitchSubscription",
            "TwitchPrimeSubscription",
            "TwitchGiftSubscription",
            "TwitchFreeFollow"
    };

    public static final String[] Currencies = { "", "RUB", "USD", "EUR", "UAH", "KZT", "BYN", "BRL", "TRY", "PLN" };

    public static final float Unlimited = 999999;

    public static final String MessageType = handlerType(MessageHandlerProperties.ImplementationId);
    public static final String CommandType = handlerType(CommandHandlerProperties.ImplementationId);

    /** Actions of this mod, in the order the menu offers them; they work even before the game registers them. */
    private static final Map<String, Class<?>> BuiltIn = new LinkedHashMap<>();
    private static final Map<String, PropertiesDescriptor<?>> BuiltInDescriptors = new ConcurrentHashMap<>();

    static {
        BuiltIn.put(MessageType, MessageHandlerProperties.class);
        BuiltIn.put(CommandType, CommandHandlerProperties.class);
        BuiltIn.put(handlerType(TitleHandlerProperties.ImplementationId), TitleHandlerProperties.class);
        BuiltIn.put(handlerType(ActionBarHandlerProperties.ImplementationId), ActionBarHandlerProperties.class);
        BuiltIn.put(handlerType(SoundHandlerProperties.ImplementationId), SoundHandlerProperties.class);
        BuiltIn.put(handlerType(ChatHandlerProperties.ImplementationId), ChatHandlerProperties.class);
    }

    public static class Action {
        /** Full id of the action, such as {@code daintegrate/handler/message}. */
        public String type;
        /** Settings as the menu edits them: field name to text, in the order of the settings class. */
        public final LinkedHashMap<String, String> fields = new LinkedHashMap<>();
        /** Delay in game ticks, as stored in the settings file. */
        public int delayTicks;
        /** Settings of an action whose mod is not installed; saved back untouched. */
        Object unknownValue;

        public Action(String type, int delayTicks) {
            this.type = type;
            this.delayTicks = delayTicks;
            for (var field : fieldsOf(type))
                fields.put(field.name, defaultText(type, field.name));
        }

        public static Action message(String text, int delayTicks) {
            var action = new Action(MessageType, delayTicks);
            action.fields.put("message", text);
            return action;
        }

        public static Action command(String text, int delayTicks) {
            var action = new Action(CommandType, delayTicks);
            action.fields.put("command", text);
            return action;
        }

        /** True when the menu knows the action's settings and can edit them. */
        public boolean isEditable() {
            return unknownValue == null && descriptorOf(type) != null;
        }

        /** The text of a message or command, or the first text setting of any other action. */
        public String text() {
            var field = mainField();
            return field == null ? "" : fields.getOrDefault(field, "");
        }

        public String mainField() {
            for (var field : fieldsOf(type))
                if (field.type == String.class)
                    return field.name;
            return null;
        }

        /** Switches the action to another type, keeping its text when both have one. */
        public void changeType(String newType) {
            if (newType.equals(type))
                return;
            var text = text();
            type = newType;
            unknownValue = null;
            fields.clear();
            for (var field : fieldsOf(newType))
                fields.put(field.name, defaultText(newType, field.name));
            var main = mainField();
            if (main != null && !text.isEmpty())
                fields.put(main, text);
        }

        public Action copy() {
            var copy = new Action(type, delayTicks);
            copy.fields.clear();
            copy.fields.putAll(fields);
            copy.unknownValue = unknownValue;
            return copy;
        }

        boolean sameAs(Action other) {
            return eq(type, other.type) && delayTicks == other.delayTicks
                    && fields.equals(other.fields) && unknownValue == other.unknownValue;
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
        model.actions.add(Action.message("{username}: {amount} {currency}! {message}", 0));
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
            if (!actions.get(i).sameAs(other.actions.get(i)))
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
            for (var value : action.fields.values())
                if (value != null && value.toLowerCase().contains(q))
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
                model.actions.add(readAction(handler));
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
            handler.properties.type = action.type;
            handler.properties.value = action.unknownValue != null ? action.unknownValue : buildValue(action);
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

    /** Every action the menu can add: this mod's first, then those other mods registered. */
    public static List<String> availableTypes() {
        var types = new ArrayList<>(BuiltIn.keySet());
        for (var descriptor : DonationAlertsIntegrate.getActions()) {
            var id = descriptor.getProperties().getUniqueId();
            if (!types.contains(id))
                types.add(id);
        }
        return types;
    }

    /** Name of an action type for the menu. */
    public static String titleOf(String type) {
        if (BuiltIn.containsKey(type))
            return I18n.t("act." + shortType(type));
        var descriptor = DonationAlertsIntegrate.getHandler(type);
        return descriptor != null ? descriptor.getTitle() : type;
    }

    /** Label of one setting of an action for the menu. */
    public static String labelOf(String type, String field) {
        var key = "act." + shortType(type) + "." + field;
        if (BuiltIn.containsKey(type) && I18n.has(key))
            return I18n.t(key);
        return field;
    }

    public static PropertiesDescriptor<?> descriptorOf(String type) {
        if (type == null)
            return null;
        var registered = DonationAlertsIntegrate.getProperties(type);
        if (registered != null)
            return registered;
        var builtIn = BuiltIn.get(type);
        if (builtIn == null)
            return null;
        return BuiltInDescriptors.computeIfAbsent(type, key ->
                new PropertiesDescriptor<>(builtIn, new EntityId(Constants.ModId, shortType(key), "handler", Constants.ModUrl)));
    }

    static Property[] fieldsOf(String type) {
        var descriptor = descriptorOf(type);
        return descriptor == null ? new Property[0] : descriptor.getProperties();
    }

    private static Action readAction(HandlerPropertiesDto handler) {
        var type = handler.properties.type;
        if (!type.contains("/"))
            type = handlerType(type);
        var value = handler.properties.value;
        var action = new Action(type, handler.delay);
        var descriptor = descriptorOf(type);
        if (descriptor == null) {
            action.unknownValue = value == null ? new LinkedHashMap<String, Object>() : value;
            return action;
        }
        var fields = descriptor.getProperties();
        if (value instanceof String text) {
            var main = action.mainField();
            if (main != null)
                action.fields.put(main, text);
            return action;
        }
        for (var field : fields) {
            Object fieldValue = null;
            if (value instanceof Map<?, ?> map)
                fieldValue = map.get(field.name);
            else if (value != null && descriptor.getType().isInstance(value)) {
                try {
                    fieldValue = descriptor.getType().getField(field.name).get(value);
                } catch (ReflectiveOperationException ignored) {
                }
            }
            if (fieldValue != null)
                action.fields.put(field.name, format(fieldValue));
        }
        return action;
    }

    private static Object buildValue(Action action) {
        var descriptor = descriptorOf(action.type);
        if (descriptor == null)
            return new LinkedHashMap<String, Object>(action.fields);
        Object value;
        try {
            var constructor = descriptor.getType().getDeclaredConstructor();
            constructor.trySetAccessible();
            value = constructor.newInstance();
        } catch (ReflectiveOperationException e) {
            return new LinkedHashMap<String, Object>(action.fields);
        }
        for (var field : descriptor.getProperties()) {
            var text = action.fields.get(field.name);
            if (text == null)
                continue;
            if (CommandType.equals(action.type) && "command".equals(field.name)) {
                text = text.trim();
                if (text.startsWith("/"))
                    text = text.substring(1);
            }
            try {
                var parsed = parse(text, field.type);
                if (parsed != null)
                    descriptor.getType().getField(field.name).set(value, parsed);
            } catch (ReflectiveOperationException | IllegalArgumentException ignored) {
                // Leave the default of the settings class
            }
        }
        return value;
    }

    /** Converts the text of a setting to its field type; null keeps the field's default. */
    static Object parse(String text, Class<?> type) {
        var trimmed = text.trim().replace(',', '.');
        try {
            if (type == String.class)
                return text;
            if (type == int.class || type == Integer.class)
                return trimmed.isEmpty() ? null : (int) Math.round(Double.parseDouble(trimmed));
            if (type == long.class || type == Long.class)
                return trimmed.isEmpty() ? null : Math.round(Double.parseDouble(trimmed));
            if (type == float.class || type == Float.class)
                return trimmed.isEmpty() ? null : Float.parseFloat(trimmed);
            if (type == double.class || type == Double.class)
                return trimmed.isEmpty() ? null : Double.parseDouble(trimmed);
            if (type == boolean.class || type == Boolean.class)
                return Boolean.parseBoolean(trimmed);
        } catch (NumberFormatException e) {
            return null;
        }
        return null;
    }

    private static String defaultText(String type, String field) {
        var descriptor = descriptorOf(type);
        if (descriptor == null)
            return "";
        try {
            var constructor = descriptor.getType().getDeclaredConstructor();
            constructor.trySetAccessible();
            var value = descriptor.getType().getField(field).get(constructor.newInstance());
            return value == null ? "" : format(value);
        } catch (ReflectiveOperationException e) {
            return "";
        }
    }

    private static String format(Object value) {
        if (value instanceof Float || value instanceof Double) {
            var number = ((Number) value).doubleValue();
            if (number == Math.rint(number) && Math.abs(number) < 1e9)
                return String.valueOf((long) number);
        }
        return String.valueOf(value);
    }

    private static String handlerType(String id) {
        return Constants.ModId + "/handler/" + id;
    }

    private static String shortType(String type) {
        return type.contains("/") ? type.substring(type.lastIndexOf('/') + 1) : type;
    }

    private static boolean eq(String a, String b) {
        return (a == null ? "" : a).equals(b == null ? "" : b);
    }
}
