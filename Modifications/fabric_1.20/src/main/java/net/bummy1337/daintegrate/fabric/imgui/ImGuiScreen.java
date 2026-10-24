package net.bummy1337.daintegrate.fabric.imgui;

import com.mojang.blaze3d.platform.InputConstants;
import imgui.ImGui;
import imgui.flag.ImGuiKey;
import net.bummy1337.daintegrate.ui.DaMenu;
import net.bummy1337.daintegrate.ui.I18n;
import net.bummy1337.daintegrate.ui.ImGuiHost;
import net.bummy1337.daintegrate.ui.UiContext;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

/**
 * Hosts the ImGui menu inside a Minecraft screen. Input comes from the screen's events
 * (the game uses SDL since 26.3, so ImGui's GLFW backend cannot be used) and the result is
 * drawn through the game's GUI renderer.
 */
public class ImGuiScreen extends Screen {
    private static ImGuiHost host;
    private static MinecraftImGuiRenderer renderer;
    private static DaMenu menu;

    private final UiContext context;
    private long lastFrame;
    private boolean textInput;
    private boolean ctrl;
    private boolean shift;
    private boolean alt;

    public ImGuiScreen(UiContext context) {
        super(Component.literal("Donation Alerts Integrate"));
        this.context = context;
        if (host == null) {
            renderer = new MinecraftImGuiRenderer();
            host = new ImGuiHost(renderer);
            var keyboard = Minecraft.getInstance().keyboardHandler;
            host.setClipboard(keyboard::getClipboard, keyboard::setClipboard);
            // The menu is kept between openings, so unsaved trigger edits survive closing it
            menu = new DaMenu(host, context);
        }
    }

    public static DaMenu menu() {
        return menu;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        var minecraft = Minecraft.getInstance();
        var window = minecraft.getWindow();
        float guiScale = (float) window.getGuiScale();
        int width = window.getWidth();
        int height = window.getHeight();

        // The vanilla screen background: the world blurred behind the menu's translucent glass
        super.extractRenderState(graphics, mouseX, mouseY, delta);

        I18n.setLanguage(minecraft.getLanguageManager().getSelected());
        var settings = context.getSettings();
        float userScale = settings == null || settings.uiScale <= 0 ? 1f : settings.uiScale;
        float autoScale = Math.max(1f, Math.min(2.5f, height / 900f));
        host.prepare(autoScale * userScale);

        long now = System.nanoTime();
        float deltaSeconds = lastFrame == 0 ? 1f / 60f : (now - lastFrame) / 1_000_000_000f;
        lastFrame = now;

        host.beginFrame(width, height, deltaSeconds);
        menu.render(width, height);
        host.endFrame();
        renderer.submit(graphics, ImGui.getDrawData(), guiScale);

        boolean wantsText = ImGui.getIO().getWantTextInput();
        if (wantsText != textInput) {
            textInput = wantsText;
            minecraft.textInputManager().onTextInputFocusChange(this, wantsText);
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_ESCAPE && !ImGui.isAnyItemActive() && !ImGui.isPopupOpen("", imgui.flag.ImGuiPopupFlags.AnyPopupId)) {
            onClose();
            return true;
        }
        key(event.key(), true);
        return true;
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        key(event.key(), false);
        return true;
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        ImGui.getIO().addInputCharacter(event.codepoint());
        return true;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        moveMouse(event.x(), event.y());
        int button = button(event.button());
        if (button >= 0)
            ImGui.getIO().addMouseButtonEvent(button, true);
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        moveMouse(event.x(), event.y());
        int button = button(event.button());
        if (button >= 0)
            ImGui.getIO().addMouseButtonEvent(button, false);
        return true;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        moveMouse(event.x(), event.y());
        return true;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        moveMouse(mouseX, mouseY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        moveMouse(mouseX, mouseY);
        ImGui.getIO().addMouseWheelEvent((float) scrollX, (float) scrollY);
        return true;
    }

    @Override
    public void removed() {
        if (textInput) {
            textInput = false;
            Minecraft.getInstance().textInputManager().onTextInputFocusChange(this, false);
        }
        // Release everything so no key stays "held" inside ImGui while the menu is closed
        var io = ImGui.getIO();
        io.addFocusEvent(false);
        ctrl = shift = alt = false;
        lastFrame = 0;
    }

    @Override
    public void onClose() {
        Minecraft.getInstance().setScreen(null);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void key(int key, boolean down) {
        var io = ImGui.getIO();
        if (KeyMap.isControl(key))
            ctrl = down;
        else if (KeyMap.isShift(key))
            shift = down;
        else if (KeyMap.isAlt(key))
            alt = down;
        io.addKeyEvent(ImGuiKey.ImGuiMod_Ctrl, ctrl);
        io.addKeyEvent(ImGuiKey.ImGuiMod_Shift, shift);
        io.addKeyEvent(ImGuiKey.ImGuiMod_Alt, alt);
        int imguiKey = KeyMap.toImGui(key);
        if (imguiKey != ImGuiKey.None)
            io.addKeyEvent(imguiKey, down);
    }

    private static void moveMouse(double guiX, double guiY) {
        float scale = (float) Minecraft.getInstance().getWindow().getGuiScale();
        ImGui.getIO().addMousePosEvent((float) guiX * scale, (float) guiY * scale);
    }

    private static int button(int minecraftButton) {
        if (minecraftButton == InputConstants.MOUSE_BUTTON_LEFT)
            return 0;
        if (minecraftButton == InputConstants.MOUSE_BUTTON_RIGHT)
            return 1;
        if (minecraftButton == InputConstants.MOUSE_BUTTON_MIDDLE)
            return 2;
        return -1;
    }
}
