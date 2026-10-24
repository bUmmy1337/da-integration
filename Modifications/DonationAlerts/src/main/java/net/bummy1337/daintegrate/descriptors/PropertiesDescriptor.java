package net.bummy1337.daintegrate.descriptors;

import net.bummy1337.daintegrate.EntityId;
import net.bummy1337.daintegrate.IHandler;
import net.bummy1337.daintegrate.Property;
import net.bummy1337.daintegrate.sensitives.ISensitive;

import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;

public class PropertiesDescriptor<T> {
    private final Class<T> type;
    private final EntityId id;
    private final Property[] properties;

    public PropertiesDescriptor(IHandler<T> handler, EntityId id) {
        this(getType(handler), id);
    }

    public PropertiesDescriptor(ISensitive<T> sensitive, EntityId id) {
        this(getType(sensitive), id);
    }

    public PropertiesDescriptor(Class<T> type, EntityId id) {
        this.type = type;
        this.id = id;
        var list = new ArrayList<Property>();
        for (var field : type.getDeclaredFields()) {
            // Constants such as ImplementationId are not settings
            if (Modifier.isStatic(field.getModifiers()) || !Modifier.isPublic(field.getModifiers()))
                continue;
            var property = new Property();
            property.name = field.getName();
            property.type = field.getType();
            list.add(property);
        }
        properties = list.toArray(new Property[0]);
    }

    @SuppressWarnings("unchecked")
    private static <T> Class<T> getType(Object object) {
        return (Class<T>)((ParameterizedType)object
                .getClass()
                .getGenericInterfaces()[0])
                .getActualTypeArguments()[0];
    }

    public String getUniqueId() {
        return id.getUniqueId();
    }

    public EntityId getId() {
        return id;
    }

    public Class<T> getType() {
        return type;
    }

    /** The public instance fields of the settings class, in declaration order. */
    public Property[] getProperties() {
        return properties;
    }
}
