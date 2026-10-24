package net.bummy1337.daintegrate.ui;

import imgui.ImGui;
import imgui.flag.ImDrawFlags;
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

        renderSidebar(220 * s, height);
        ImGui.sameLine(0, 0);
        renderContent();

        ImGui.end();

        if (ImGui.getIO().getKeyCtrl() && ImGui.isKeyPressed(ImGuiKey.S, false))
            triggers.save();
    }

    private void renderSidebar(float width, float height) {
        float s = scale();
        var origin = ImGui.getCursorScreenPos();
        float rounding = ImGui.getStyle().getWindowRounding();
        ImGui.getWindowDrawList().addRectFilled(origin.x, origin.y, origin.x + width, origin.y + height, col(Theme.Sidebar), rounding,
                ImDrawFlags.RoundCornersLeft);
        ImGui.pushStyleColor(ImGuiCol.ChildBg, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 14 * s, 18 * s);
        ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, 0);
        ImGui.beginChild("##sidebar", width, height, imgui.flag.ImGuiChildFlags.AlwaysUseWindowPadding, ImGuiWindowFlags.NoScrollbar);
        ImGui.popStyleVar(2);

        renderLogo();
        Widgets.gap(18);

        navItem(Page.Overview, Icons.Kind.Home);
        navItem(Page.Connection, Icons.Kind.Plug);
        navItem(Page.Triggers, Icons.Kind.Bolt);
        navItem(Page.Settings, Icons.Kind.Gear);
        navItem(Page.Help, Icons.Kind.Question);

        // Connection summary pinned to the bottom of the sidebar
        var client = context.getClient();
        var state = client.getState();
        float boxHeight = 92 * s;
        ImGui.setCursorPosY(height - boxHeight - 18 * s);
        ImGui.pushStyleColor(ImGuiCol.ChildBg, col(Theme.Surface));
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 12 * s, 12 * s);
        ImGui.beginChild("##sidebar-status", 0, boxHeight, imgui.flag.ImGuiChildFlags.Borders | imgui.flag.ImGuiChildFlags.AlwaysUseWindowPadding, ImGuiWindowFlags.NoScrollbar);
        ImGui.popStyleVar();
        Widgets.statusDot(Widgets.stateColor(state), state != ConnectionState.Disconnected && state != ConnectionState.Error);
        ImGui.text(I18n.t("state." + state.name()));
        float buttonWidth = ImGui.getContentRegionAvailX();
        if (state == ConnectionState.Disconnected || state == ConnectionState.Error) {
            if (Widgets.primaryButton(I18n.t("conn.connect"), buttonWidth))
                connection.connectOrOpen();
        } else if (Widgets.button(I18n.t("conn.disconnect"), buttonWidth)) {
            client.disconnect();
        }
        ImGui.endChild();
        ImGui.popStyleColor();

        ImGui.endChild();
        ImGui.popStyleColor();

        // Separator line between sidebar and content
        var min = ImGui.getItemRectMin();
        var max = ImGui.getItemRectMax();
        ImGui.getWindowDrawList().addLine(max.x, min.y, max.x, max.y, col(Theme.Border), 1f);
    }

    private void renderLogo() {
        float s = scale();
        var draw = ImGui.getWindowDrawList();
        var pos = ImGui.getCursorScreenPos();
        float size = 34 * s;
        draw.addRectFilled(pos.x, pos.y, pos.x + size, pos.y + size, col(Theme.Accent), 10 * s);
        Icons.draw(draw, Icons.Kind.Heart, pos.x + size / 2, pos.y + size / 2, 15 * s, col(0xFFFFFF));
        ImGui.dummy(size, size);
        ImGui.sameLine(0, 10 * s);
        ImGui.beginGroup();
        Widgets.bold("DA Integrate");
        ImGui.pushStyleColor(ImGuiCol.Text, col(Theme.TextMuted));
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
        float height = 38 * s;

        ImGui.pushID(target.name());
        boolean clicked = ImGui.invisibleButton("##nav", width, height);
        boolean hovered = ImGui.isItemHovered();
        ImGui.popID();
        if (hovered)
            ImGui.setMouseCursor(ImGuiMouseCursor.Hand);

        if (selected)
            draw.addRectFilled(pos.x, pos.y, pos.x + width, pos.y + height, col(Theme.Accent, 0.14f), 9 * s);
        else if (hovered)
            draw.addRectFilled(pos.x, pos.y, pos.x + width, pos.y + height, col(Theme.SurfaceHover, 0.7f), 9 * s);
        if (selected)
            draw.addRectFilled(pos.x, pos.y + 9 * s, pos.x + 3 * s, pos.y + height - 9 * s, col(Theme.Accent), 2 * s);

        int textColor = selected ? Theme.Text : hovered ? Theme.Text : Theme.TextSecondary;
        int iconColor = selected ? Theme.Accent : textColor;
        Icons.draw(draw, icon, pos.x + 22 * s, pos.y + height / 2, 16 * s, col(iconColor));
        draw.addText(pos.x + 42 * s, pos.y + (height - ImGui.getTextLineHeight()) / 2, col(textColor), label);

        if (target == Page.Triggers && triggers.isDirty())
            draw.addCircleFilled(pos.x + width - 14 * s, pos.y + height / 2, 3.5f * s, col(Theme.Accent));

        if (clicked)
            page = target;
        ImGui.dummy(0, 2 * s);
    }

    private void renderContent() {
        float s = scale();
        ImGui.pushStyleColor(ImGuiCol.ChildBg, 0);
        ImGui.pushStyleVar(ImGuiStyleVar.WindowPadding, 28 * s, 24 * s);
        ImGui.beginChild("##content", 0, 0, imgui.flag.ImGuiChildFlags.AlwaysUseWindowPadding, 0);
        ImGui.popStyleVar();

        renderCloseButton();

        switch (page) {
            case Overview -> overview.render();
            case Connection -> connection.render();
            case Triggers -> triggers.render();
            case Settings -> settings.render();
            case Help -> help.render();
        }

        ImGui.endChild();
        ImGui.popStyleColor();
    }

    private void renderCloseButton() {
        float s = scale();
        float size = 30 * s;
        var cursor = ImGui.getCursorPos();
        ImGui.setCursorPos(ImGui.getWindowWidth() - size - 14 * s, 14 * s);
        var draw = ImGui.getWindowDrawList();
        var pos = ImGui.getCursorScreenPos();
        boolean clicked = ImGui.invisibleButton("##close", size, size);
        boolean hovered = ImGui.isItemHovered();
        if (hovered) {
            draw.addRectFilled(pos.x, pos.y, pos.x + size, pos.y + size, col(Theme.SurfaceHover), 8 * s);
            ImGui.setMouseCursor(ImGuiMouseCursor.Hand);
            ImGui.setTooltip(I18n.t("close") + " (Esc)");
        }
        Icons.draw(draw, Icons.Kind.Close, pos.x + size / 2, pos.y + size / 2, 12 * s, col(hovered ? Theme.Text : Theme.TextSecondary));
        ImGui.setCursorPos(cursor.x, cursor.y);
        if (clicked)
            context.close();
    }
}
