package net.bummy1337.daintegrate.ui;

import imgui.ImGui;
import imgui.flag.ImGuiInputTextFlags;
import imgui.flag.ImGuiKey;
import net.bummy1337.daintegrate.Constants;
import net.bummy1337.daintegrate.TokenStore;
import net.bummy1337.dontaionalerts.ConnectionState;

import java.io.IOException;

/**
 * Guided three-step connection: open Donation Alerts, paste the token or widget link, connect.
 */
class ConnectionPage {
    private final DaMenu menu;
    private final UiContext context;
    private String draft = "";
    private String feedback;
    private boolean feedbackIsError;
    private boolean editing;

    ConnectionPage(DaMenu menu, UiContext context) {
        this.menu = menu;
        this.context = context;
    }

    /** Connects with the saved token, or opens this page when there is none yet. */
    void connectOrOpen() {
        var token = context.getTokenStore().load();
        if (token == null) {
            menu.open(DaMenu.Page.Connection);
            return;
        }
        context.getClient().connect(token);
    }

    void render() {
        float s = DaMenu.scale();
        Widgets.title(I18n.t("conn.title"), I18n.t("conn.subtitle"));

        var store = context.getTokenStore();
        var token = store.load();
        var client = context.getClient();
        var state = client.getState();
        boolean hasToken = token != null;
        boolean connected = state == ConnectionState.Connected;

        float columnWidth = Math.min(ImGui.getContentRegionAvailX(), 720 * s);

        // Step 1
        if (Widgets.beginCard("##step1", columnWidth, 0)) {
            stepHeader(1, hasToken, !hasToken, I18n.t("conn.step1"));
            Widgets.muted(I18n.t("conn.step1.desc"));
            Widgets.gap(4);
            if (hasToken ? Widgets.button(I18n.t("conn.step1.button"), 0) : Widgets.primaryButton(I18n.t("conn.step1.button"), 0))
                context.openUrl(Constants.DonationAlertsTokenPage);
            ImGui.sameLine();
            if (Widgets.button(I18n.t("conn.step1.widget"), 0))
                context.openUrl(Constants.DonationAlertsWidgetPage);
        }
        Widgets.endCard();

        // Step 2
        if (Widgets.beginCard("##step2", columnWidth, 0)) {
            stepHeader(2, hasToken && !editing, !hasToken || editing, I18n.t("conn.step2"));
            if (hasToken && !editing) {
                Widgets.statusDot(Theme.Success, false, true);
                ImGui.alignTextToFramePadding();
                ImGui.text(I18n.t("conn.step2.saved", TokenStore.mask(token)));
                ImGui.sameLine();
                if (Widgets.button(I18n.t("conn.step2.change"), 0)) {
                    editing = true;
                    draft = "";
                    feedback = null;
                }
                ImGui.sameLine();
                if (Widgets.dangerButton(I18n.t("conn.step2.forget"), 0)) {
                    client.disconnect();
                    store.delete();
                    feedback = null;
                }
            } else {
                Widgets.muted(I18n.t("conn.step2.desc"));
                Widgets.gap(4);
                if (Widgets.primaryButton(I18n.t("conn.step2.paste"), 0))
                    accept(context.getClipboard(), true);
                ImGui.sameLine();
                float saveWidth = 110 * s;
                float inputWidth = ImGui.getContentRegionAvailX() - saveWidth - ImGui.getStyle().getItemSpacingX();
                draft = Widgets.input("##token", I18n.t("conn.step2.hint"), draft, inputWidth, 512,
                        ImGuiInputTextFlags.Password);
                boolean submitted = ImGui.isItemDeactivated() && ImGui.isKeyPressed(ImGuiKey.Enter, false);
                ImGui.sameLine();
                if (Widgets.button(I18n.t("conn.step2.save"), saveWidth) || submitted)
                    accept(draft, false);
                if (editing && hasToken && Widgets.link(I18n.t("cancel"))) {
                    editing = false;
                    feedback = null;
                }
            }
            if (feedback != null)
                Widgets.colored(feedbackIsError ? Theme.Danger : Theme.Success, feedback);
        }
        Widgets.endCard();

        // Step 3
        if (Widgets.beginCard("##step3", columnWidth, 0)) {
            stepHeader(3, connected && client.getLastEventAt() > 0, hasToken, I18n.t("conn.step3"));
            Widgets.muted(I18n.t("conn.step3.desc"));
            Widgets.gap(4);

            ImGui.beginDisabled(!hasToken);
            switch (state) {
                case Disconnected -> {
                    if (Widgets.primaryButton(I18n.t("conn.connect"), 180 * s))
                        client.connect(token);
                }
                case Error -> {
                    if (Widgets.primaryButton(I18n.t("conn.retry"), 180 * s))
                        client.connect(token);
                }
                default -> {
                    if (Widgets.button(I18n.t("conn.disconnect"), 180 * s))
                        client.disconnect();
                }
            }
            ImGui.endDisabled();
            if (!hasToken)
                Widgets.tooltip(I18n.t("conn.needToken"));

            ImGui.sameLine(0, 16 * s);
            ImGui.alignTextToFramePadding();
            Widgets.statusPill(state);

            switch (state) {
                case Connected -> {
                    if (client.getLastEventAt() > 0)
                        Widgets.colored(Theme.TextSecondary, I18n.t("conn.lastEvent", Widgets.ago(client.getLastEventAt())));
                    else
                        Widgets.colored(Theme.TextSecondary, I18n.t("conn.waiting"));
                }
                case Error, Reconnecting -> {
                    if (client.getLastError() != null)
                        Widgets.colored(Theme.Danger, I18n.t("conn.error", client.getLastError()));
                }
                default -> {
                }
            }

            Widgets.gap(4);
            var settings = context.getSettings();
            if (settings != null) {
                boolean auto = !settings.disableAutoConnect;
                if (ImGui.checkbox(I18n.t("conn.auto"), auto)) {
                    settings.disableAutoConnect = auto;
                    try {
                        context.saveSettings(settings);
                    } catch (IOException e) {
                        showFeedback(I18n.t("trg.saveError", e.getMessage()), true);
                    }
                }
            }
        }
        Widgets.endCard();
    }

    private void accept(String input, boolean fromClipboard) {
        if (input == null || input.isBlank()) {
            showFeedback(I18n.t(fromClipboard ? "conn.step2.clipboard.empty" : "conn.step2.invalid"), true);
            return;
        }
        var token = TokenStore.parse(input);
        if (token == null) {
            showFeedback(I18n.t("conn.step2.invalid"), true);
            return;
        }
        try {
            context.getTokenStore().save(token);
        } catch (IOException e) {
            showFeedback(I18n.t("trg.saveError", e.getMessage()), true);
            return;
        }
        draft = "";
        editing = false;
        showFeedback(null, false);
        context.getClient().connect(token);
    }

    private void showFeedback(String text, boolean error) {
        feedback = text;
        feedbackIsError = error;
    }

    private static void stepHeader(int number, boolean done, boolean current, String title) {
        float s = DaMenu.scale();
        Widgets.stepNumber(number, done, current);
        ImGui.sameLine(0, 12 * s);
        ImGui.setCursorPosY(ImGui.getCursorPosY() + 4 * s);
        Widgets.heading(title);
        Widgets.gap(2);
    }
}
