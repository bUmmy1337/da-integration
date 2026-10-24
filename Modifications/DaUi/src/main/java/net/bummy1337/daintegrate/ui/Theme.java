package net.bummy1337.daintegrate.ui;

import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;

/**
 * Dark theme with the Donation Alerts orange as the accent colour.
 * Colours are packed as 0xRRGGBB and converted to ImGui's ABGR on use.
 */
public final class Theme {
    public static final int Background = 0x0F1115;
    public static final int Sidebar = 0x13161B;
    public static final int Surface = 0x181B21;
    public static final int SurfaceRaised = 0x1F232A;
    public static final int SurfaceHover = 0x272C35;
    public static final int Border = 0x2A2F38;
    public static final int Text = 0xE8EAED;
    public static final int TextSecondary = 0xA2A8B3;
    public static final int TextMuted = 0x6B7280;
    public static final int Accent = 0xF57D07;
    public static final int AccentHover = 0xFF9330;
    public static final int AccentActive = 0xD96A00;
    public static final int Success = 0x34D399;
    public static final int Warning = 0xFBBF24;
    public static final int Danger = 0xF87171;
    public static final int Info = 0x60A5FA;
    public static final int Purple = 0xA78BFA;

    private Theme() {
    }

    /** Converts 0xRRGGBB plus alpha (0..1) into ImGui's packed ABGR colour. */
    public static int col(int rgb, float alpha) {
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int a = Math.round(Math.max(0, Math.min(1, alpha)) * 255);
        return (a << 24) | (b << 16) | (g << 8) | r;
    }

    public static int col(int rgb) {
        return col(rgb, 1f);
    }

    public static void apply(float scale) {
        ImGuiStyle style = ImGui.getStyle();
        style.setWindowPadding(20, 20);
        style.setFramePadding(10, 7);
        style.setItemSpacing(10, 10);
        style.setItemInnerSpacing(8, 6);
        style.setCellPadding(10, 7);
        style.setIndentSpacing(18);
        style.setScrollbarSize(10);
        style.setWindowRounding(14);
        style.setChildRounding(12);
        style.setFrameRounding(8);
        style.setPopupRounding(10);
        style.setScrollbarRounding(8);
        style.setGrabRounding(8);
        style.setTabRounding(8);
        style.setWindowBorderSize(1);
        style.setChildBorderSize(1);
        style.setPopupBorderSize(1);
        style.setFrameBorderSize(0);
        style.setWindowMinSize(32, 32);
        style.setGrabMinSize(12);
        style.setButtonTextAlign(0.5f, 0.5f);
        style.setSelectableTextAlign(0f, 0.5f);
        if (scale != 1f)
            style.scaleAllSizes(scale);

        set(style, ImGuiCol.Text, Text, 1f);
        set(style, ImGuiCol.TextDisabled, TextMuted, 1f);
        set(style, ImGuiCol.WindowBg, Background, 1f);
        set(style, ImGuiCol.ChildBg, Surface, 1f);
        set(style, ImGuiCol.PopupBg, SurfaceRaised, 0.98f);
        set(style, ImGuiCol.Border, Border, 1f);
        set(style, ImGuiCol.BorderShadow, 0, 0f);
        set(style, ImGuiCol.FrameBg, SurfaceRaised, 1f);
        set(style, ImGuiCol.FrameBgHovered, SurfaceHover, 1f);
        set(style, ImGuiCol.FrameBgActive, SurfaceHover, 1f);
        set(style, ImGuiCol.TitleBg, Sidebar, 1f);
        set(style, ImGuiCol.TitleBgActive, Sidebar, 1f);
        set(style, ImGuiCol.TitleBgCollapsed, Sidebar, 1f);
        set(style, ImGuiCol.MenuBarBg, Sidebar, 1f);
        set(style, ImGuiCol.ScrollbarBg, 0, 0f);
        set(style, ImGuiCol.ScrollbarGrab, Border, 1f);
        set(style, ImGuiCol.ScrollbarGrabHovered, SurfaceHover, 1f);
        set(style, ImGuiCol.ScrollbarGrabActive, TextMuted, 1f);
        set(style, ImGuiCol.CheckMark, Accent, 1f);
        set(style, ImGuiCol.SliderGrab, Accent, 1f);
        set(style, ImGuiCol.SliderGrabActive, AccentHover, 1f);
        set(style, ImGuiCol.Button, SurfaceRaised, 1f);
        set(style, ImGuiCol.ButtonHovered, SurfaceHover, 1f);
        set(style, ImGuiCol.ButtonActive, Border, 1f);
        set(style, ImGuiCol.Header, Accent, 0.16f);
        set(style, ImGuiCol.HeaderHovered, SurfaceHover, 1f);
        set(style, ImGuiCol.HeaderActive, Accent, 0.24f);
        set(style, ImGuiCol.Separator, Border, 1f);
        set(style, ImGuiCol.SeparatorHovered, Accent, 0.6f);
        set(style, ImGuiCol.SeparatorActive, Accent, 1f);
        set(style, ImGuiCol.ResizeGrip, 0, 0f);
        set(style, ImGuiCol.ResizeGripHovered, Accent, 0.5f);
        set(style, ImGuiCol.ResizeGripActive, Accent, 1f);
        set(style, ImGuiCol.Tab, SurfaceRaised, 1f);
        set(style, ImGuiCol.TabHovered, SurfaceHover, 1f);
        set(style, ImGuiCol.TabSelected, Accent, 0.22f);
        set(style, ImGuiCol.TableHeaderBg, SurfaceRaised, 1f);
        set(style, ImGuiCol.TableBorderStrong, Border, 1f);
        set(style, ImGuiCol.TableBorderLight, Border, 0.6f);
        set(style, ImGuiCol.TableRowBg, 0, 0f);
        set(style, ImGuiCol.TableRowBgAlt, 0xFFFFFF, 0.02f);
        set(style, ImGuiCol.TextSelectedBg, Accent, 0.35f);
        set(style, ImGuiCol.NavCursor, Accent, 0.8f);
        set(style, ImGuiCol.ModalWindowDimBg, 0x000000, 0.55f);
    }

    private static void set(ImGuiStyle style, int index, int rgb, float alpha) {
        style.setColor(index, ((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f, alpha);
    }
}
