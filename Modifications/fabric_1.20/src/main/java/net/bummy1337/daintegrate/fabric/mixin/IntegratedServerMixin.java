package net.bummy1337.daintegrate.fabric.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.server.IntegratedServer;
import net.minecraft.server.players.NameAndId;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * The game takes the world owner's name from the profile the session service returns for the player's UUID,
 * but the player joins under the launcher's name. When the two differ (some launchers), the host is treated
 * as a guest and loses operator rights even with cheats on. Also count the player who logged in as the launcher's user.
 */
@Mixin(IntegratedServer.class)
public abstract class IntegratedServerMixin {
    @Inject(method = "isSingleplayerOwner", at = @At("RETURN"), cancellable = true)
    private void daintegrate$launcherUserIsOwner(NameAndId player, CallbackInfoReturnable<Boolean> result) {
        if (result.getReturnValueZ())
            return;
        var user = Minecraft.getInstance().getUser();
        if (player.id().equals(user.getProfileId()) && player.name().equalsIgnoreCase(user.getName()))
            result.setReturnValue(true);
    }
}
