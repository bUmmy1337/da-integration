package net.bummy1337.daintegrate.fabric.gui.clickgui.impl.trigger;

import net.bummy1337.daintegrate.configurations.TriggerDto;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.trigger.handler.TriggerAnimationHandler;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.trigger.handler.TriggerScrollHandler;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.trigger.render.SettingsPanelRenderer;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.trigger.render.TriggerListRenderer;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.trigger.util.TriggerDisplayHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.*;

public class TriggerComponent {

    private List<TriggerDto> triggers = new ArrayList<>();
    private List<TriggerDto> displayTriggers = new ArrayList<>();
    private TriggerDto selectedTrigger = null;

    private String currentCategory = null;

    private final TriggerAnimationHandler animationHandler;
    private final TriggerScrollHandler scrollHandler;
    private final TriggerDisplayHelper displayHelper;
    private final TriggerListRenderer listRenderer;
    private final SettingsPanelRenderer settingsRenderer;

    private int savedGuiScale = 1;
    private float lastMouseX = 0, lastMouseY = 0;
    private float lastListX = 0, lastListY = 0, lastListWidth = 0, lastListHeight = 0;

    public TriggerComponent() {
        this.animationHandler = new TriggerAnimationHandler();
        this.scrollHandler = new TriggerScrollHandler();
        this.displayHelper = new TriggerDisplayHelper();
        this.listRenderer = new TriggerListRenderer(animationHandler, displayHelper);
        this.settingsRenderer = new SettingsPanelRenderer(animationHandler);
    }

    public void updateTriggers(List<TriggerDto> newTriggers, String category) {
        animationHandler.prepareTransition(triggers, displayTriggers);
        currentCategory = category;
        triggers = newTriggers;
        rebuildDisplayList();

        scrollHandler.resetTriggerScroll();
        animationHandler.initTriggerAnimations(displayTriggers);
        displayHelper.updateTriggersWithSettings(displayTriggers);

        if (animationHandler.shouldScrollToTrigger()) {
            animationHandler.clearScrollTarget();
        } else if (!displayTriggers.isEmpty() && (selectedTrigger == null || !displayTriggers.contains(selectedTrigger))) {
            selectTrigger(displayTriggers.get(0));
        } else if (displayTriggers.isEmpty()) {
            selectedTrigger = null;
        }
    }

    private void rebuildDisplayList() {
        displayTriggers.clear();
        displayTriggers.addAll(triggers);
    }

    public void selectTriggerFromSearch(TriggerDto trigger) {
        animationHandler.setScrollTarget(trigger);
    }

    public void scrollToTriggerAndHighlight(TriggerDto trigger) {
        if (trigger == null || !displayTriggers.contains(trigger)) return;

        selectTrigger(trigger);
        int triggerIndex = displayTriggers.indexOf(trigger);
        if (triggerIndex >= 0 && scrollHandler.getLastTriggerListHeight() > 0) {
            scrollHandler.scrollToTrigger(triggerIndex, displayTriggers.size());
        }
        animationHandler.startHighlight(trigger);
    }

    public void selectTrigger(TriggerDto trigger) {
        if (trigger == selectedTrigger) return;

        selectedTrigger = trigger;
        scrollHandler.resetSettingScroll();
    }

    public void renderTriggerList(GuiGraphicsExtractor g, float x, float y, float width, float height,
                                  float mouseX, float mouseY, int guiScale, float alphaMultiplier) {
        lastMouseX = mouseX;
        lastMouseY = mouseY;
        lastListX = x;
        lastListY = y;
        lastListWidth = width;
        lastListHeight = height;

        animationHandler.updateAll(displayTriggers, selectedTrigger, mouseX, mouseY, x, y, width, height,
                (float) scrollHandler.getTriggerDisplayScroll());
        listRenderer.render(g, displayTriggers, selectedTrigger, x, y, width, height,
                mouseX, mouseY, guiScale, alphaMultiplier, animationHandler, scrollHandler);
    }

    public void renderSettingsPanel(GuiGraphicsExtractor g, float x, float y, float width, float height,
                                    float mouseX, float mouseY, float delta, int guiScale, float alphaMultiplier) {
        savedGuiScale = guiScale;
        settingsRenderer.render(g, selectedTrigger, x, y, width, height,
                mouseX, mouseY, delta, guiScale, alphaMultiplier, scrollHandler, animationHandler);
    }

    public void updateScroll(float delta, float scrollSpeed) {
        scrollHandler.update(delta);
    }

    public void updateScrollFades(float delta, float scrollSpeed, float triggerListHeight, float settingsPanelHeight) {
        scrollHandler.updateFades(displayTriggers.size(), calculateTotalSettingHeight(), triggerListHeight, settingsPanelHeight);
    }

    public float calculateTotalSettingHeight() {
        return settingsRenderer.calculateTotalHeight(selectedTrigger);
    }

    public TriggerDto getTriggerAtPosition(double mouseX, double mouseY, float listX, float listY,
                                           float listWidth, float listHeight) {
        return listRenderer.getTriggerAtPosition(displayTriggers, mouseX, mouseY, listX, listY, listWidth, listHeight,
                scrollHandler.getTriggerDisplayScroll(), animationHandler.isCategoryTransitioning());
    }

    public void handleTriggerScroll(double vertical, float listHeight) {
        if (animationHandler.isCategoryTransitioning()) return;
        scrollHandler.handleTriggerScroll(vertical, listHeight, displayTriggers.size());
    }

    public void handleSettingScroll(double vertical, float panelHeight) {
        scrollHandler.handleSettingScroll(vertical, panelHeight, calculateTotalSettingHeight());
    }

    public boolean isTransitioning() {
        return animationHandler.isCategoryTransitioning();
    }

    public List<TriggerDto> getDisplayTriggers() { return displayTriggers; }
    public TriggerDto getSelectedTrigger() { return selectedTrigger; }
    public TriggerAnimationHandler getAnimationHandler() { return animationHandler; }
    public TriggerScrollHandler getScrollHandler() { return scrollHandler; }
}
