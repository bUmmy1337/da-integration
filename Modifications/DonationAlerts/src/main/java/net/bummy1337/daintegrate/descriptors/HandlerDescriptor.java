package net.bummy1337.daintegrate.descriptors;

import net.bummy1337.daintegrate.IHandler;

public class HandlerDescriptor<T> {
    private final IHandler<T> handler;
    private final PropertiesDescriptor<T> properties;
    private final String title;

    public HandlerDescriptor(IHandler<T> handler, PropertiesDescriptor<T> propertiesDescriptor) {
        this(handler, propertiesDescriptor, null);
    }

    public HandlerDescriptor(IHandler<T> handler, PropertiesDescriptor<T> propertiesDescriptor, String title) {
        this.handler = handler;
        this.properties = propertiesDescriptor;
        this.title = title;
    }

    public IHandler<T> getHandler() {
        return handler;
    }

    public PropertiesDescriptor<T> getProperties() {
        return properties;
    }

    /** Name shown in the menu; the action's id when the mod gave none. */
    public String getTitle() {
        return title == null || title.isBlank() ? properties.getId().getEntityId() : title;
    }
}
