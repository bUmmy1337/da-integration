package net.bummy1337.daintegrate.api;

import net.bummy1337.daintegrate.IModuleConfiguration;

/**
 * Entry point for mods that add their own actions to Donation Alerts Integrate.
 * Declare the class in your fabric.mod.json:
 * <pre>
 * "entrypoints": {
 *   "daintegrate": [ "com.example.MyDonationActions" ]
 * }
 * </pre>
 * It is called once, before the settings are loaded, so triggers that use your actions load too.
 */
public interface DaIntegratePlugin {
    void register(IModuleConfiguration configuration);
}
