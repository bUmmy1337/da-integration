package net.bummy1337.daintegrate.ui;

import imgui.ImGui;
import imgui.flag.ImDrawFlags;
import imgui.flag.ImGuiChildFlags;
import imgui.flag.ImGuiCol;
import imgui.flag.ImGuiCond;
import imgui.flag.ImGuiKey;
import imgui.flag.ImGuiMouseCursor;
import imgui.flag.ImGuiStyleVar;
import imgui.flag.ImGuiWindowFlags;
import net.bummy1337.dontaionalerts.ConnectionState;

import static net.bummy1337.daintegrate.ui.Theme.col;

/**
 * The mod's main window: a sidebar with sections and the selected page on the right.
 * Only builds ImGui commands; the platform layer owns the frame and the drawing.
 */
public class DaMenu {
    public enum Page { Overview, Connection, Triggers, Settings, Help }

    private static ImGuiHost currentHost;

    private final ImGuiHost host;
    private final UiContext context;
    private final OverviewPage overview;
    private final ConnectionPage connection;
    private final TriggersPage triggers;
    private final SettingsPage settings;
    private final HelpPage help;
    private Page page;

    public DaMenu(ImGuiHost host, UiContext context) {
        this.host = host;
        this.context = context;
        this.overview = new OverviewPage(this, context);
        this.connection = new ConnectionPage(this, context);
        this.triggers = new TriggersPage(this, context);
        this.settings = new SettingsPage(this, context);
        this.help = new HelpPage(this, context);
        this.page = context.getTokenStore().exists() ? Page.Overview : Page.Connection;
    }

    public static float scale() {
        return currentHost == null ? 1f : currentHost.scale();
    }

    public static ImGuiHost fonts() {
        return currentHost;
    }

    public void open(Page page) {
        this.page = page;
    }

    public Page getPage() {
        return page;
    }

    public boolean hasUnsavedChanges() {
        return triggers.isDirty();
    }

    /** Builds the whole menu for one frame. */
    public void render(float displayWidth, float displayHeight) {
        currentHost = host;
        float s = scale();

        float width = Math.min(displayWidth - 32 * s, 1080 * s);
        float height = Math.min(displayHeight - 32 * s, 700 * s);
        ImGui.setNextWindowPos(displayWidth / 2, displayHeight / 2, ImGuiCond.Always, 0.5f, 0.5f);
        ImGui.setNextWindowSize(width, height, ImGuiCond.Always);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 0, 0);
        ImGui.begin("##da-menu", ImGuiWindowFlags.NoDecoration | ImGuiWindowFlags.NoMove | ImGuiWindowFlags.NoSavedSettings
                | ImGuiWindowFlags.NoBringToFrontOnFocus);
        ImGui.popStyleVar();

        renderSidebar(230 * s, height);
        ImGui.sameLine(0, 0);
        renderContent();

        ImGui.end();

        if (ImGui.getIO().getKeyCtrl() && ImGui.isKeyPressed(ImGuiKey.S, false))
            triggers.save();

        renderToasts(context.getSettings(), displayWidth, displayHeight, host, s);
    }

    /** Draws the pop-up notifications; the game calls it on its own when the menu is closed. */
    public static void renderToasts(net.bummy1337.daintegrate.configurations.SettingsDto settings, float displayWidth, float displayHeight,
                                    ImGuiHost host, float scale) {
        if (settings != null && settings.disableToasts) {
            Toasts.clear();
            return;
        }
        Toasts.render(host, displayWidth, displayHeight, scale, Toasts.Corner.parse(settings == null ? null : settings.toastCorner));
    }

    private void renderSidebar(float width, float height) {
        float s = scale();
        var draw = ImGui.getWindowDrawList();
        var origin = ImGui.getCursorScreenPos();
        float rounding = ImGui.getStyle().getWindowRounding();
        draw.addRectFilled(origin.x, origin.y, origin.x + width, origin.y + height, col(Theme.Black, Theme.SidebarAlpha), rounding,
                ImDrawFlags.RoundCornersLeft);
        // Hairline that fades out at both ends
        float lineX = origin.x + width;
        float third = height / 3;
        draw.addRectFilledMultiColor(lineX - 1, origin.y, lineX, origin.y + third,
                Theme.white(0), Theme.white(0), Theme.white(0.1f), Theme.white(0.1f));
        draw.addRectFilled(lineX - 1, origin.y + third, lineX, origin.y + third * 2, Theme.white(0.1f));
        draw.addRectFilledMultiColor(lineX - 1, origin.y + third * 2, lineX, origin.y + height,
                Theme.white(0.1f), Theme.white(0.1f), Theme.white(0), Theme.white(0));

        ImGui.pushStyleColor(ImGuiCol.ChildBg, 0);
        ImGui.pushStyleColor(ImGuiCol.Border, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 16 * s, 20 * s);
        ImGui.beginChild("##sidebar", width, height, ImGuiChildFlags.AlwaysUseWindowPadding, ImGuiWindowFlags.NoScrollbar);
        ImGui.popStyleVar();

        renderLogo();
        Widgets.gap(22);
        Widgets.caption(I18n.t("nav.section"));
        Widgets.gap(2);

        navItem(Page.Overview, Icons.Kind.Home);
        navItem(Page.Connection, Icons.Kind.Plug);
        navItem(Page.Triggers, Icons.Kind.Bolt);
        navItem(Page.Settings, Icons.Kind.Gear);
        navItem(Page.Help, Icons.Kind.Question);

        renderStatusWidget(height);

        ImGui.endChild();
        ImGui.popStyleColor(2);
    }

    /** Small glass card at the bottom of the sidebar, in the spirit of a system status widget. */
    private void renderStatusWidget(float sidebarHeight) {
        float s = scale();
        var client = context.getClient();
        var state = client.getState();
        float boxHeight = 104 * s;
        ImGui.setCursorPosY(sidebarHeight - boxHeight - 20 * s);
        ImGui.pushStyleColor(ImGuiCol.ChildBg, Theme.white(Theme.CardAlpha));
        ImGui.pushStyleColor(ImGuiCol.Border, Theme.white(Theme.BorderAlpha));
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 14 * s, 12 * s);
        ImGui.beginChild("##sidebar-status", 0, boxHeight, ImGuiChildFlags.Borders | ImGuiChildFlags.AlwaysUseWindowPadding, ImGuiWindowFlags.NoScrollbar);
        ImGui.popStyleVar();

        Widgets.caption("Donation Alerts");
        Widgets.statusDot(Widgets.stateColor(state), state == ConnectionState.Connected || state == ConnectionState.Connecting || state == ConnectionState.Reconnecting);
        ImGui.text(I18n.t("state." + state.name()));
        float buttonWidth = ImGui.getContentRegionAvailX();
        if (state == ConnectionState.Disconnected || state == ConnectionState.Error) {
            if (Widgets.primaryButton(I18n.t("conn.connect"), buttonWidth))
                connection.connectOrOpen();
        } else if (Widgets.button(I18n.t("conn.disconnect"), buttonWidth)) {
            client.disconnect();
        }
        ImGui.endChild();
        ImGui.popStyleColor(2);
    }

    private void renderLogo() {
        float s = scale();
        var draw = ImGui.getWindowDrawList();
        var pos = ImGui.getCursorScreenPos();
        float size = 40 * s;
        // Soft glow under the app icon
        for (int i = 3; i >= 1; i--)
            draw.addRectFilled(pos.x - i * 2 * s, pos.y - i * 2 * s + 3 * s, pos.x + size + i * 2 * s, pos.y + size + i * 2 * s + 3 * s,
                    col(Theme.Accent, 0.05f), 12 * s + i * 2 * s);
        draw.addImageRounded(host.logo(size), pos.x, pos.y, pos.x + size, pos.y + size, 0, 0, 1, 1, col(Theme.White), 11 * s);
        draw.addRect(pos.x, pos.y, pos.x + size, pos.y + size, Theme.white(0.12f), 11 * s);
        ImGui.dummy(size, size);
        ImGui.sameLine(0, 12 * s);
        ImGui.beginGroup();
        ImGui.setCursorPosY(ImGui.getCursorPosY() + 2 * s);
        Widgets.bold("DA Integrate");
        ImGui.setCursorPosY(ImGui.getCursorPosY() - 8 * s);
        ImGui.pushStyleColor(ImGuiCol.Text, Theme.white(Theme.TextMutedAlpha));
        ImGui.text(I18n.t("logo.subtitle"));
        ImGui.popStyleColor();
        ImGui.endGroup();
    }

    private void navItem(Page target, Icons.Kind icon) {
        float s = scale();
        boolean selected = page == target;
        var label = I18n.t("nav." + target.name().toLowerCase());
        var draw = ImGui.getWindowDrawList();
        var pos = ImGui.getCursorScreenPos();
        float width = ImGui.getContentRegionAvailX();
        float height = 40 * s;

        ImGui.pushID(target.name());
        boolean clicked = ImGui.invisibleButton("##nav", width, height);
        boolean hovered = ImGui.isItemHovered();
        ImGui.popID();
        if (hovered)
            ImGui.setMouseCursor(ImGuiMouseCursor.Hand);

        if (selected) {
            draw.addRectFilled(pos.x, pos.y, pos.x + width, pos.y + height, Theme.white(0.09f), 12 * s);
            draw.addRect(pos.x, pos.y, pos.x + width, pos.y + height, Theme.white(0.08f), 12 * s);
        } else if (hovered) {
            draw.addRectFilled(pos.x, pos.y, pos.x + width, pos.y + height, Theme.white(0.05f), 12 * s);
        }

        int textColor = selected || hovered ? Theme.white(Theme.TextAlpha) : Theme.white(Theme.TextSecondaryAlpha);
        // iOS-style icon tile
        float tile = 26 * s;
        float tileX = pos.x + 8 * s;
        float tileY = pos.y + (height - tile) / 2;
        if (selected)
            draw.addRectFilled(tileX, tileY, tileX + tile, tileY + tile, col(Theme.Accent), 8 * s);
        else
            draw.addRectFilled(tileX, tileY, tileX + tile, tileY + tile, Theme.white(0.07f), 8 * s);
        Icons.draw(draw, icon, tileX + tile / 2, tileY + tile / 2, 13 * s, selected ? col(Theme.White) : textColor);
        draw.addText(tileX + tile + 12 * s, pos.y + (height - ImGui.getTextLineHeight()) / 2, textColor, label);

        if (target == Page.Triggers && triggers.isDirty())
            draw.addCircleFilled(pos.x + width - 14 * s, pos.y + height / 2, 3.5f * s, col(Theme.Accent));

        if (clicked)
            page = target;
        ImGui.dummy(0, 1 * s);
    }

    private void renderContent() {
        float s = scale();
        ImGui.pushStyleColor(ImGuiCol.ChildBg, 0);
        ImGui.pushStyleColor(ImGuiCol.Border, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 30 * s, 26 * s);
        ImGui.beginChild("##content", 0, 0, ImGuiChildFlags.AlwaysUseWindowPadding, 0);
        ImGui.popStyleVar();
        ImGui.popStyleColor(2);

        renderCloseButton();

        switch (page) {
            case Overview -> overview.render();
            case Connection -> connection.render();
            case Triggers -> triggers.render();
            case Settings -> settings.render();
            case Help -> help.render();
        }

        ImGui.endChild();
    }

    private void renderCloseButton() {
        float s = scale();
        float size = 30 * s;
        var cursor = ImGui.getCursorPos();
        ImGui.setCursorPos(ImGui.getWindowWidth() - size - 16 * s, 16 * s);
        var draw = ImGui.getWindowDrawList();
        var pos = ImGui.getCursorScreenPos();
        boolean clicked = ImGui.invisibleButton("##close", size, size);
        boolean hovered = ImGui.isItemHovered();
        draw.addCircleFilled(pos.x + size / 2, pos.y + size / 2, size / 2, Theme.white(hovered ? 0.14f : 0.07f));
        if (hovered) {
            ImGui.setMouseCursor(ImGuiMouseCursor.Hand);
            ImGui.setTooltip(I18n.t("close") + " (Esc)");
        }
        Icons.draw(draw, Icons.Kind.Close, pos.x + size / 2, pos.y + size / 2, 9 * s, Theme.white(hovered ? 0.9f : 0.55f));
        ImGui.setCursorPos(cursor.x, cursor.y);
        if (clicked)
            context.close();
    }
}
