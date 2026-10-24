package net.bummy1337.daintegrate.fabric.imgui;

import imgui.ImGui;
import net.bummy1337.daintegrate.configurations.SettingsDto;
import net.bummy1337.daintegrate.ui.DaMenu;
import net.bummy1337.daintegrate.ui.I18n;
import net.bummy1337.daintegrate.ui.ImGuiHost;
import net.bummy1337.daintegrate.ui.Toasts;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** The one ImGui context of the game, shared by the menu screen and the on-screen notifications. */
public final class ImGuiRuntime {
    private static ImGuiHost host;
    private static MinecraftImGuiRenderer renderer;
    private static boolean menuOpen;
    private static long lastFrame;

    private ImGuiRuntime() {
    }

    static ImGuiHost host() {
        if (host == null) {
            renderer = new MinecraftImGuiRenderer();
            host = new ImGuiHost(renderer);
            var keyboard = Minecraft.getInstance().keyboardHandler;
            host.setClipboard(keyboard::getClipboard, keyboard::setClipboard);
        }
        return host;
    }

    static MinecraftImGuiRenderer renderer() {
        host();
        return renderer;
    }

    static void setMenuOpen(boolean open) {
        menuOpen = open;
    }

    /** Effective menu scale: follows the window height, times the user's setting. */
    static float scale(SettingsDto settings, int windowHeight) {
        float user = settings == null || settings.uiScale <= 0 ? 1f : settings.uiScale;
        float auto = Math.max(1f, Math.min(2.5f, windowHeight / 900f));
        return auto * user;
    }

    /** Seconds since the previous ImGui frame, whoever drew it. */
    static float frameDelta() {
        long now = System.nanoTime();
        float delta = lastFrame == 0 || now - lastFrame > 1_000_000_000L ? 1f / 60f : (now - lastFrame) / 1_000_000_000f;
        lastFrame = now;
        return delta;
    }

    /**
     * Draws the notifications over the game. The menu draws them itself while it is open,
     * so this only runs a frame when there is something to show.
     */
    public static void renderHud(GuiGraphicsExtractor graphics, SettingsDto settings) {
        if (menuOpen || Toasts.isIdle() || (settings != null && settings.disableToasts))
            return;
        var minecraft = Minecraft.getInstance();
        var window = minecraft.getWindow();
        int width = window.getWidth();
        int height = window.getHeight();
        float scale = scale(settings, height);

        I18n.setLanguage(minecraft.getLanguageManager().getSelected());
        var host = host();
        host.prepare(scale);
        host.beginFrame(width, height, frameDelta());
        DaMenu.renderToasts(settings, width, height, host, scale);
        host.endFrame();
        renderer().submit(graphics, ImGui.getDrawData(), (float) window.getGuiScale());
    }
}
