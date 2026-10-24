package net.bummy1337.daintegrate.fabric.imgui;

import com.mojang.blaze3d.platform.InputConstants;
import imgui.flag.ImGuiKey;

import java.util.HashMap;
import java.util.Map;

/** Minecraft key codes (the physical keys of {@code KeyEvent#key}) to ImGui keys. */
final class KeyMap {
    private static final Map<Integer, Integer> keys = new HashMap<>();

    private KeyMap() {
    }

    static int toImGui(int key) {
        return keys.getOrDefault(key, ImGuiKey.None);
    }

    static boolean isControl(int key) {
        return key == InputConstants.KEY_LCONTROL || key == InputConstants.KEY_RCONTROL;
    }

    static boolean isShift(int key) {
        return key == InputConstants.KEY_LSHIFT || key == InputConstants.KEY_RSHIFT;
    }

    static boolean isAlt(int key) {
        return key == InputConstants.KEY_LALT || key == InputConstants.KEY_RALT;
    }

    static {
        keys.put(InputConstants.KEY_TAB, ImGuiKey.Tab);
        keys.put(InputConstants.KEY_LEFT, ImGuiKey.LeftArrow);
        keys.put(InputConstants.KEY_RIGHT, ImGuiKey.RightArrow);
        keys.put(InputConstants.KEY_UP, ImGuiKey.UpArrow);
        keys.put(InputConstants.KEY_DOWN, ImGuiKey.DownArrow);
        keys.put(InputConstants.KEY_PAGEUP, ImGuiKey.PageUp);
        keys.put(InputConstants.KEY_PAGEDOWN, ImGuiKey.PageDown);
        keys.put(InputConstants.KEY_HOME, ImGuiKey.Home);
        keys.put(InputConstants.KEY_END, ImGuiKey.End);
        keys.put(InputConstants.KEY_INSERT, ImGuiKey.Insert);
        keys.put(InputConstants.KEY_DELETE, ImGuiKey.Delete);
        keys.put(InputConstants.KEY_BACKSPACE, ImGuiKey.Backspace);
        keys.put(InputConstants.KEY_SPACE, ImGuiKey.Space);
        keys.put(InputConstants.KEY_RETURN, ImGuiKey.Enter);
        keys.put(InputConstants.KEY_NUMPADENTER, ImGuiKey.KeypadEnter);
        keys.put(InputConstants.KEY_ESCAPE, ImGuiKey.Escape);
        keys.put(InputConstants.KEY_LCONTROL, ImGuiKey.LeftCtrl);
        keys.put(InputConstants.KEY_RCONTROL, ImGuiKey.RightCtrl);
        keys.put(InputConstants.KEY_LSHIFT, ImGuiKey.LeftShift);
        keys.put(InputConstants.KEY_RSHIFT, ImGuiKey.RightShift);
        keys.put(InputConstants.KEY_LALT, ImGuiKey.LeftAlt);
        keys.put(InputConstants.KEY_RALT, ImGuiKey.RightAlt);
        keys.put(InputConstants.KEY_A, ImGuiKey.A);
        keys.put(InputConstants.KEY_C, ImGuiKey.C);
        keys.put(InputConstants.KEY_S, ImGuiKey.S);
        keys.put(InputConstants.KEY_V, ImGuiKey.V);
        keys.put(InputConstants.KEY_X, ImGuiKey.X);
        keys.put(InputConstants.KEY_Y, ImGuiKey.Y);
        keys.put(InputConstants.KEY_Z, ImGuiKey.Z);
    }
}
