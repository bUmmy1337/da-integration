package net.bummy1337.daintegrate.fabric.gui.clickgui.impl.trigger.handler;

public class TriggerScrollHandler {
    private double triggerTargetScroll = 0, triggerDisplayScroll = 0;
    private double settingTargetScroll = 0, settingDisplayScroll = 0;
    private float triggerScrollTopFade = 0f, triggerScrollBottomFade = 0f;
    private float settingScrollTopFade = 0f, settingScrollBottomFade = 0f;

    private float lastSettingsPanelHeight = 0f;
    private float lastTriggerListHeight = 0f;
    private long lastScrollUpdateTime = System.currentTimeMillis();

    private static final float SCROLL_SPEED = 12f;
    private static final float FADE_SPEED = 8f;
    private static final float CORNER_INSET = 3f;
    private static final float TRIGGER_ITEM_HEIGHT = 22f;

    public double getTriggerTargetScroll() { return triggerTargetScroll; }
    public double getTriggerDisplayScroll() { return triggerDisplayScroll; }
    public double getSettingTargetScroll() { return settingTargetScroll; }
    public double getSettingDisplayScroll() { return settingDisplayScroll; }
    public float getTriggerScrollTopFade() { return triggerScrollTopFade; }
    public float getTriggerScrollBottomFade() { return triggerScrollBottomFade; }
    public float getSettingScrollTopFade() { return settingScrollTopFade; }
    public float getSettingScrollBottomFade() { return settingScrollBottomFade; }
    public float getLastSettingsPanelHeight() { return lastSettingsPanelHeight; }
    public float getLastTriggerListHeight() { return lastTriggerListHeight; }

    public void resetTriggerScroll() {
        triggerTargetScroll = triggerDisplayScroll = 0;
    }

    public void resetSettingScroll() {
        settingTargetScroll = settingDisplayScroll = 0;
    }

    public void update(float delta) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((currentTime - lastScrollUpdateTime) / 1000f, 0.1f);
        lastScrollUpdateTime = currentTime;

        triggerDisplayScroll = smoothScroll(triggerDisplayScroll, triggerTargetScroll, deltaTime);
        settingDisplayScroll = smoothScroll(settingDisplayScroll, settingTargetScroll, deltaTime);
    }

    private double smoothScroll(double current, double target, float deltaTime) {
        double diff = target - current;
        if (Math.abs(diff) < 0.5) return target;
        return current + diff * SCROLL_SPEED * deltaTime;
    }

    public void updateFades(int triggerCount, float totalSettingHeight, float triggerListHeight, float settingsPanelHeight) {
        lastSettingsPanelHeight = settingsPanelHeight;
        lastTriggerListHeight = triggerListHeight;

        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((currentTime - lastScrollUpdateTime) / 1000f, 0.1f);

        float maxTriggerScroll = Math.max(0, triggerCount * 24f - triggerListHeight + 10);
        float maxSettingScroll = Math.max(0, totalSettingHeight - settingsPanelHeight + 45);

        triggerScrollTopFade = updateFade(triggerScrollTopFade, triggerDisplayScroll < -0.5f, deltaTime);
        triggerScrollBottomFade = updateFade(triggerScrollBottomFade, triggerDisplayScroll > -maxTriggerScroll + 0.5f && maxTriggerScroll > 0, deltaTime);
        settingScrollTopFade = updateFade(settingScrollTopFade, settingDisplayScroll < -0.5f, deltaTime);
        settingScrollBottomFade = updateFade(settingScrollBottomFade, settingDisplayScroll > -maxSettingScroll + 0.5f && maxSettingScroll > 0, deltaTime);
    }

    private float updateFade(float current, boolean condition, float deltaTime) {
        float target = condition ? 1f : 0f;
        float diff = target - current;
        if (Math.abs(diff) < 0.01f) return target;
        return current + diff * FADE_SPEED * deltaTime;
    }

    public void handleTriggerScroll(double vertical, float listHeight, int triggerCount) {
        float effectiveHeight = listHeight - CORNER_INSET * 2 - 2;
        float maxScroll = Math.max(0, triggerCount * 24f - effectiveHeight + 10);
        triggerTargetScroll = Math.max(-maxScroll, Math.min(0, triggerTargetScroll + vertical * 25));
    }

    public void handleSettingScroll(double vertical, float panelHeight, float totalSettingHeight) {
        float effectiveHeight = panelHeight - 31 - CORNER_INSET - 3;
        float maxScroll = Math.max(0, totalSettingHeight - effectiveHeight + 10);
        settingTargetScroll = Math.max(-maxScroll, Math.min(0, settingTargetScroll + vertical * 25));
    }

    public void scrollToTrigger(int triggerIndex, int totalTriggers) {
        float triggerY = triggerIndex * (TRIGGER_ITEM_HEIGHT + 2);
        float visibleHeight = lastTriggerListHeight - CORNER_INSET * 2 - 4;
        float centerOffset = (visibleHeight - TRIGGER_ITEM_HEIGHT) / 2f;
        float targetScroll = -(triggerY - centerOffset);

        float maxScroll = Math.max(0, totalTriggers * (TRIGGER_ITEM_HEIGHT + 2) - visibleHeight);
        targetScroll = Math.max(-maxScroll, Math.min(0, targetScroll));

        triggerTargetScroll = targetScroll;
    }

    public void correctSettingScrollPosition(float totalSettingHeight) {
        if (lastSettingsPanelHeight <= 0) return;

        float maxScroll = Math.max(0, totalSettingHeight - lastSettingsPanelHeight + 45);
        if (settingTargetScroll < -maxScroll) {
            settingTargetScroll = -maxScroll;
        }
        if (settingDisplayScroll < -maxScroll) {
            settingDisplayScroll = -maxScroll;
        }
    }
}
