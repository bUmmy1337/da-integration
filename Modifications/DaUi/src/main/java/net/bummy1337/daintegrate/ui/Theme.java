package net.bummy1337.daintegrate.ui;

import imgui.ImGui;
import imgui.ImGuiStyle;
import imgui.flag.ImGuiCol;

/**
 * "Glass" theme: translucent dark surfaces over the game's blurred background,
 * hairline white borders, white text at a few opacities and the logo's orange as accent.
 * Colours are 0xRRGGBB plus a separate alpha, converted to ImGui's ABGR by {@link #col}.
 */
public final class Theme {
    public static final int Black = 0x000000;
    public static final int White = 0xFFFFFF;

    /** Window glass: black at this alpha over the blurred world. */
    public static final int Glass = 0x101116;
    public static final float WindowAlpha = 0.80f;
    public static final float SidebarAlpha = 0.30f;
    /** Cards: white at this alpha on top of the window glass. */
    public static final float CardAlpha = 0.035f;
    public static final float CardHoverAlpha = 0.07f;
    public static final float FieldAlpha = 0.06f;
    public static final float BorderAlpha = 0.08f;

    public static final float TextAlpha = 0.92f;
    public static final float TextSecondaryAlpha = 0.55f;
    public static final float TextMutedAlpha = 0.32f;

    public static final int Accent = 0xFF8A00;
    public static final int AccentLight = 0xFFB01F;
    public static final int AccentDark = 0xF26A00;
    public static final int Success = 0x34D399;
    public static final int Warning = 0xFBBF24;
    public static final int Danger = 0xFF5A5F;
    public static final int Info = 0x60A5FA;
    public static final int Purple = 0xA78BFA;

    // Opaque equivalents for places that need a solid colour (popups, combo lists)
    public static final int Popup = 0x1C1C1E;
    public static final int Text = 0xEDEDED;
    public static final int TextSecondary = 0x8E8E93;
    public static final int TextMuted = 0x5A5A5E;

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

    public static int white(float alpha) {
        return col(White, alpha);
    }

    private static float px(float size, float scale) {
        return (float) Math.floor(size * scale);
    }

    public static void apply(float scale) {
        ImGuiStyle style = ImGui.getStyle();
        // Sizes are set scaled rather than with scaleAllSizes: that one scales the current style again on
        // every scale change, so sizes this method does not reset shrank each time until ImGui asserted
        // (WindowBorderHoverPadding > 0) and the game closed
        style.setWindowPadding(px(20, scale), px(20, scale));
        style.setFramePadding(px(12, scale), px(8, scale));
        style.setItemSpacing(px(10, scale), px(10, scale));
        style.setItemInnerSpacing(px(8, scale), px(6, scale));
        style.setCellPadding(px(12, scale), px(8, scale));
        style.setIndentSpacing(px(18, scale));
        style.setScrollbarSize(px(8, scale));
        style.setWindowRounding(px(18, scale));
        style.setChildRounding(px(14, scale));
        style.setFrameRounding(px(10, scale));
        style.setPopupRounding(px(14, scale));
        style.setScrollbarRounding(px(8, scale));
        style.setGrabRounding(px(10, scale));
        style.setTabRounding(px(10, scale));
        style.setWindowBorderSize(1);
        style.setChildBorderSize(1);
        style.setPopupBorderSize(1);
        style.setFrameBorderSize(0);
        style.setWindowMinSize(px(32, scale), px(32, scale));
        style.setGrabMinSize(px(14, scale));
        style.setButtonTextAlign(0.5f, 0.5f);
        style.setSelectableTextAlign(0f, 0.5f);

        set(style, ImGuiCol.Text, White, TextAlpha);
        set(style, ImGuiCol.TextDisabled, White, TextMutedAlpha);
        set(style, ImGuiCol.WindowBg, Glass, WindowAlpha);
        set(style, ImGuiCol.ChildBg, White, CardAlpha);
        set(style, ImGuiCol.PopupBg, Popup, 0.97f);
        set(style, ImGuiCol.Border, White, BorderAlpha);
        set(style, ImGuiCol.BorderShadow, Black, 0f);
        set(style, ImGuiCol.FrameBg, White, FieldAlpha);
        set(style, ImGuiCol.FrameBgHovered, White, 0.10f);
        set(style, ImGuiCol.FrameBgActive, White, 0.12f);
        set(style, ImGuiCol.TitleBg, Black, 0.6f);
        set(style, ImGuiCol.TitleBgActive, Black, 0.6f);
        set(style, ImGuiCol.TitleBgCollapsed, Black, 0.6f);
        set(style, ImGuiCol.MenuBarBg, Black, 0.3f);
        set(style, ImGuiCol.ScrollbarBg, Black, 0f);
        set(style, ImGuiCol.ScrollbarGrab, White, 0.15f);
        set(style, ImGuiCol.ScrollbarGrabHovered, White, 0.25f);
        set(style, ImGuiCol.ScrollbarGrabActive, White, 0.35f);
        set(style, ImGuiCol.CheckMark, Accent, 1f);
        set(style, ImGuiCol.SliderGrab, White, 0.95f);
        set(style, ImGuiCol.SliderGrabActive, White, 1f);
        set(style, ImGuiCol.Button, White, 0.08f);
        set(style, ImGuiCol.ButtonHovered, White, 0.13f);
        set(style, ImGuiCol.ButtonActive, White, 0.18f);
        set(style, ImGuiCol.Header, White, 0.08f);
        set(style, ImGuiCol.HeaderHovered, White, 0.10f);
        set(style, ImGuiCol.HeaderActive, White, 0.14f);
        set(style, ImGuiCol.Separator, White, BorderAlpha);
        set(style, ImGuiCol.SeparatorHovered, Accent, 0.6f);
        set(style, ImGuiCol.SeparatorActive, Accent, 1f);
        set(style, ImGuiCol.ResizeGrip, Black, 0f);
        set(style, ImGuiCol.ResizeGripHovered, Accent, 0.5f);
        set(style, ImGuiCol.ResizeGripActive, Accent, 1f);
        set(style, ImGuiCol.Tab, White, 0.06f);
        set(style, ImGuiCol.TabHovered, White, 0.12f);
        set(style, ImGuiCol.TabSelected, White, 0.14f);
        set(style, ImGuiCol.TableHeaderBg, White, 0f);
        set(style, ImGuiCol.TableBorderStrong, White, BorderAlpha);
        set(style, ImGuiCol.TableBorderLight, White, 0.06f);
        set(style, ImGuiCol.TableRowBg, Black, 0f);
        set(style, ImGuiCol.TableRowBgAlt, White, 0.02f);
        set(style, ImGuiCol.TextSelectedBg, Accent, 0.35f);
        set(style, ImGuiCol.NavCursor, Accent, 0.8f);
        set(style, ImGuiCol.ModalWindowDimBg, Black, 0.45f);
    }

    private static void set(ImGuiStyle style, int index, int rgb, float alpha) {
        style.setColor(index, ((rgb >> 16) & 0xFF) / 255f, ((rgb >> 8) & 0xFF) / 255f, (rgb & 0xFF) / 255f, alpha);
    }
}
