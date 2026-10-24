package net.bummy1337.daintegrate;

import net.bummy1337.daintegrate.listeners.IListener;
import net.bummy1337.daintegrate.sensitives.ISensitive;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;

public interface IModuleConfiguration {
    <T> IModuleConfiguration registerHandler(IHandler<T> handler);

    /**
     * Adds an action that triggers can run. It shows up in the menu under {@code title}, and is stored
     * in the settings file as {@code <modId>/handler/<id>}.
     *
     * @param propertiesType settings of the action, see {@link IAction}; use {@link None} when it has none
     */
    <T> IModuleConfiguration registerAction(String id, String title, Class<T> propertiesType, IAction<T> action);

    <T> IModuleConfiguration registerSensitive(ISensitive<T> sensitive);
    IModuleConfiguration registerEventListener(IListener<ReadOnlyDonationAlertsEvent> event);
}
