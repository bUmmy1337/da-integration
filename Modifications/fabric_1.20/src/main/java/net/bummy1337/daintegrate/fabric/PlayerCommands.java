package net.bummy1337.daintegrate.fabric;

import net.minecraft.client.Minecraft;
import net.minecraft.server.permissions.LevelBasedPermissionSet;

/**
 * Runs a command for the player. In singleplayer the command goes straight to the built-in server with
 * operator rights, so it works in a world with cheats off. On a real server it is sent as the player and needs OP there.
 */
final class PlayerCommands {
    private PlayerCommands() {
    }

    /** The command without the leading slash. */
    static void run(String command) {
        var minecraft = Minecraft.getInstance();
        var player = minecraft.player;
        if (player == null)
            return;
        var server = minecraft.getSingleplayerServer();
        if (server == null) {
            player.connection.sendCommand(command);
            return;
        }
        var uuid = player.getUUID();
        server.execute(() -> {
            var serverPlayer = server.getPlayerList().getPlayer(uuid);
            if (serverPlayer == null)
                return;
            var source = serverPlayer.createCommandSourceStack().withPermission(LevelBasedPermissionSet.OWNER);
            server.getCommands().performPrefixedCommand(source, command);
        });
    }
}
