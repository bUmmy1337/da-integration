package net.bummy1337.daintegrate;

import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;

/**
 * Something a trigger does, registered by this mod or by another one through
 * {@link IModuleConfiguration#registerAction}. Runs on the game thread.
 *
 * @param <T> the action's settings: a class with public fields of type String, int, float or boolean,
 *            which the menu shows as a form and the settings file stores under the action
 */
@FunctionalInterface
public interface IAction<T> {
    void run(ReadOnlyDonationAlertsEvent event, T properties);
}
