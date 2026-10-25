package net.bummy1337.exampleaddon;

import net.bummy1337.daintegrate.IModuleConfiguration;
import net.bummy1337.daintegrate.ReplaceHelper;
import net.bummy1337.daintegrate.api.DaIntegratePlugin;
import net.bummy1337.dontaionalerts.ReadOnlyDonationAlertsEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Called by Donation Alerts Integrate once, before it loads its settings.
 * Everything registered here shows up in the menu under "+ More…".
 */
public class ExampleAddon implements DaIntegratePlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger("daintegrate-example-addon");

    @Override
    public void register(IModuleConfiguration configuration) {
        configuration
                // Stored in the settings file as daintegrate-example-addon/handler/hearts
                .registerAction("hearts", "Hearts around me", HeartsSettings.class, ExampleAddon::hearts)
                .registerAction("thanks", "Thank-you on screen", ThanksSettings.class, ExampleAddon::thanks)
                // Hears every donation, subscription and Twitch event, whatever the triggers are
                .registerEventListener(event ->
                        LOGGER.info("{} sent {} {}", event.getUserName(), event.getAmount(), event.getCurrency()));
    }

    // Actions run on the game thread, so they can use the game directly
    private static void hearts(ReadOnlyDonationAlertsEvent event, HeartsSettings settings) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null || minecraft.level == null)
            return;
        int count = Math.clamp(settings.count, 1, 200);
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2 * i / count;
            double x = player.getX() + Math.cos(angle) * settings.radius;
            double z = player.getZ() + Math.sin(angle) * settings.radius;
            double y = player.getY() + 1 + (i % 3) * 0.4;
            minecraft.level.addParticle(ParticleTypes.HEART, x, y, z, 0, 0.05, 0);
        }
    }

    private static void thanks(ReadOnlyDonationAlertsEvent event, ThanksSettings settings) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null)
            return;
        // {username}, {amount}, {currency}, {message} and {playername}, like in the mod's own actions
        var text = Component.literal(ReplaceHelper.replace(settings.text, event, player.getName().getString()));
        if (settings.big) {
            minecraft.gui.hud.setTimes(10, 60, 20);
            minecraft.gui.hud.setSubtitle(Component.empty());
            minecraft.gui.hud.setTitle(text);
        } else {
            minecraft.gui.hud.setOverlayMessage(text, false);
        }
    }
}
