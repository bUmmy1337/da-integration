package net.bummy1337.daintegrate;

import net.bummy1337.daintegrate.configurations.TriggerDto;
import net.bummy1337.daintegrate.descriptors.HandlerDescriptor;
import net.bummy1337.daintegrate.descriptors.PropertiesDescriptor;
import net.bummy1337.daintegrate.descriptors.SensitiveDescriptor;
import net.bummy1337.daintegrate.listeners.IListener;
import net.bummy1337.daintegrate.sensitives.ISensitive;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Stream;

public class DonationAlertsIntegrate {
    public static final DonationAlertsIntegrate Instance = new DonationAlertsIntegrate();

    private ConcurrentHashMap<String, PropertiesDescriptor> entities = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, HandlerDescriptor> handlers = new ConcurrentHashMap<>();
    private CopyOnWriteArrayList<HandlerDescriptor> handlerOrder = new CopyOnWriteArrayList<>();
    private ConcurrentHashMap<String, SensitiveDescriptor> sensitives = new ConcurrentHashMap<>();
    // Written by the settings file watcher, read by the game thread when a donation comes
    private volatile TriggerDto[] triggers;
    private volatile boolean skipTestDonation;
    private CopyOnWriteArrayList<IListener<ReadOnlyDonationAlertsEvent>> listeners = new CopyOnWriteArrayList<>();

    public DonationAlertsIntegrate()
    {
    }

    public static IModuleConfiguration configure(String modId, String modUrl) {
        return new ModuleConfiguration(modId, modUrl);
    }

    static <T extends IHandler> void registerHandler(T handler, String modId, String modUrl) {
        var descriptor = new PropertiesDescriptor<T>(handler, new EntityId(modId, handler.getImplementationId(), "handler", modUrl));
        Instance.entities.put(descriptor.getUniqueId(), descriptor);
        putHandler(new HandlerDescriptor(handler, descriptor));
    }

    static <T> void registerAction(String id, String title, Class<T> propertiesType, IAction<T> action, String modId, String modUrl) {
        var descriptor = new PropertiesDescriptor<T>(propertiesType, new EntityId(modId, id, "handler", modUrl));
        IHandler<T> handler = new IHandler<>() {
            @Override
            public void handle(ReadOnlyDonationAlertsEvent event, T properties) {
                action.run(event, properties);
            }

            @Override
            public String getImplementationId() {
                return id;
            }
        };
        Instance.entities.put(descriptor.getUniqueId(), descriptor);
        putHandler(new HandlerDescriptor<>(handler, descriptor, title));
    }

    private static void putHandler(HandlerDescriptor descriptor) {
        var previous = Instance.handlers.put(descriptor.getProperties().getUniqueId(), descriptor);
        if (previous != null)
            Instance.handlerOrder.remove(previous);
        Instance.handlerOrder.add(descriptor);
    }

    static <T extends ISensitive> void registerSensitive(T sensitive, String modId, String modUrl) {
        var descriptor = new PropertiesDescriptor<T>(sensitive, new EntityId(modId, sensitive.getImplementationId(), "sensitive", modUrl));
        Instance.entities.put(descriptor.getUniqueId(), descriptor);
        Instance.sensitives.put(descriptor.getUniqueId(), new SensitiveDescriptor(sensitive, descriptor));
    }

    public static void registerEventListener(IListener<ReadOnlyDonationAlertsEvent> listener) {
        Instance.listeners.add(listener);
    }

    public static Iterator<HandlerDescriptor> getHandler() {
        return Instance.handlers.elements().asIterator();
    }

    /** Every registered action, in the order mods registered them. */
    public static List<HandlerDescriptor> getActions() {
        return List.copyOf(Instance.handlerOrder);
    }

    public static Iterator<IListener<ReadOnlyDonationAlertsEvent>> getEventListeners() {
        return Instance.listeners.iterator();
    }

    public static boolean knownEntity(String id) {
        return Instance.entities.containsKey(id);
    }

    public static PropertiesDescriptor getProperties(String id) {
        return Instance.entities.get(id);
    }

    public static SensitiveDescriptor getSensitive(String id) {
        return Instance.sensitives.get(id);
    }

    public static HandlerDescriptor getHandler(String id) {
        return Instance.handlers.get(id);
    }

    public static Stream<TriggerDto> getTriggers() {
        var triggers = Instance.triggers;
        return triggers == null ? Stream.empty() : Arrays.stream(triggers);
    }

    public static boolean isSkipTestDonation() {
        return Instance.skipTestDonation;
    }

    void updateTriggers(TriggerDto[] triggers) {
        this.triggers = triggers;
    }

    void setSkipTestDonation(boolean value) {
        this.skipTestDonation = value;
    }
}
