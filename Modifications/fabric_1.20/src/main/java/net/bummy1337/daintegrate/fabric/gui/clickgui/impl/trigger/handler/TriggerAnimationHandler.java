package net.bummy1337.daintegrate.fabric.gui.clickgui.impl.trigger.handler;

import net.bummy1337.daintegrate.configurations.TriggerDto;

import java.util.*;

public class TriggerAnimationHandler {

    private Map<TriggerDto, Float> triggerAnimations = new HashMap<>();
    private Map<TriggerDto, Long> triggerAnimStartTimes = new HashMap<>();
    private Map<TriggerDto, Float> oldTriggerAnimations = new HashMap<>();

    private Map<TriggerDto, Float> hoverAnimations = new HashMap<>();
    private Map<TriggerDto, Float> stateAnimations = new HashMap<>();
    private Map<TriggerDto, Float> selectedIconAnimations = new HashMap<>();
    private Map<TriggerDto, Float> positionAnimations = new HashMap<>();
    private Map<TriggerDto, Float> triggerAlphaAnimations = new HashMap<>();

    private List<TriggerDto> oldTriggers = new ArrayList<>();
    private double oldTriggerDisplayScroll = 0;

    private float selectedPulseAnimation = 0f;
    private long lastHoverUpdateTime = System.currentTimeMillis();
    private long lastStateUpdateTime = System.currentTimeMillis();
    private long lastIconUpdateTime = System.currentTimeMillis();

    private TriggerDto highlightedTrigger = null;
    private long highlightStartTime = 0;
    private float highlightAnimation = 0f;

    private boolean scrollToTrigger = false;
    private TriggerDto scrollTargetTrigger = null;

    private boolean isCategoryTransitioning = false;
    private float categoryTransitionProgress = 1f;
    private long categoryTransitionStartTime = 0;

    private static final float TRIGGER_ANIM_DURATION = 300f;
    private static final float CATEGORY_TRANSITION_DURATION = 280f;
    private static final float HIGHLIGHT_DURATION = 2000f;
    private static final float HOVER_ANIM_SPEED = 8f;
    private static final float STATE_ANIM_SPEED = 10f;
    private static final float ICON_ANIM_SPEED = 10f;
    private static final float POSITION_ANIM_SPEED = 6f;
    private static final float PULSE_SPEED = 5.5f;
    private static final float CORNER_INSET = 3f;
    private static final float TRIGGER_ITEM_HEIGHT = 22f;

    public Map<TriggerDto, Float> getTriggerAnimations() { return triggerAnimations; }
    public Map<TriggerDto, Long> getTriggerAnimStartTimes() { return triggerAnimStartTimes; }
    public Map<TriggerDto, Float> getOldTriggerAnimations() { return oldTriggerAnimations; }
    public Map<TriggerDto, Float> getHoverAnimations() { return hoverAnimations; }
    public Map<TriggerDto, Float> getStateAnimations() { return stateAnimations; }
    public Map<TriggerDto, Float> getSelectedIconAnimations() { return selectedIconAnimations; }
    public Map<TriggerDto, Float> getPositionAnimations() { return positionAnimations; }
    public Map<TriggerDto, Float> getTriggerAlphaAnimations() { return triggerAlphaAnimations; }
    public List<TriggerDto> getOldTriggers() { return oldTriggers; }
    public double getOldTriggerDisplayScroll() { return oldTriggerDisplayScroll; }
    public float getSelectedPulseAnimation() { return selectedPulseAnimation; }
    public TriggerDto getHighlightedTrigger() { return highlightedTrigger; }
    public long getHighlightStartTime() { return highlightStartTime; }
    public float getHighlightAnimation() { return highlightAnimation; }
    public boolean isCategoryTransitioning() { return isCategoryTransitioning; }
    public float getCategoryTransitionProgress() { return categoryTransitionProgress; }

    public void setOldTriggerDisplayScroll(double scroll) { this.oldTriggerDisplayScroll = scroll; }
    public void setHighlightedTrigger(TriggerDto trigger) { this.highlightedTrigger = trigger; }
    public void setHighlightStartTime(long time) { this.highlightStartTime = time; }
    public void setHighlightAnimation(float anim) { this.highlightAnimation = anim; }
    public void setIsCategoryTransitioning(boolean transitioning) { this.isCategoryTransitioning = transitioning; }
    public void setCategoryTransitionProgress(float progress) { this.categoryTransitionProgress = progress; }

    public void prepareTransition(List<TriggerDto> triggers, List<TriggerDto> displayTriggers) {
        if (!triggers.isEmpty()) {
            oldTriggers = new ArrayList<>(triggers);
            oldTriggerAnimations = new HashMap<>(triggerAnimations);
            isCategoryTransitioning = true;
            categoryTransitionStartTime = System.currentTimeMillis();
            categoryTransitionProgress = 0f;
        }
    }

    public void initTriggerAnimations(List<TriggerDto> displayTriggers) {
        triggerAnimations.clear();
        triggerAnimStartTimes.clear();
        hoverAnimations.clear();
        stateAnimations.clear();
        selectedIconAnimations.clear();

        long currentTime = System.currentTimeMillis();
        long delayBase = (long) (CATEGORY_TRANSITION_DURATION * 0.3f);

        for (int i = 0; i < displayTriggers.size(); i++) {
            TriggerDto trigger = displayTriggers.get(i);
            triggerAnimations.put(trigger, 0f);
            triggerAnimStartTimes.put(trigger, currentTime + delayBase + i * 25L);
            hoverAnimations.put(trigger, 0f);
            stateAnimations.put(trigger, trigger.isActive ? 1f : 0f);
            selectedIconAnimations.put(trigger, 0f);
            positionAnimations.put(trigger, 1f);
            triggerAlphaAnimations.put(trigger, 1f);
        }
    }

    public void updateAll(List<TriggerDto> displayTriggers, TriggerDto selectedTrigger,
                          float mouseX, float mouseY, float listX, float listY, float listWidth, float listHeight,
                          float scrollOffset) {
        updateCategoryTransition();
        updateTriggerAnimations(displayTriggers);
        updateStateAnimations(displayTriggers);
        updateSelectedIconAnimations(displayTriggers, selectedTrigger);
        updateHighlightAnimation();
        updateHoverAnimations(displayTriggers, mouseX, mouseY, listX, listY, listWidth, listHeight, scrollOffset);
    }

    private void updateCategoryTransition() {
        if (!isCategoryTransitioning) return;

        long elapsed = System.currentTimeMillis() - categoryTransitionStartTime;
        float progress = Math.min(1f, elapsed / CATEGORY_TRANSITION_DURATION);
        categoryTransitionProgress = easeOutCubic(progress);

        if (progress >= 1f) {
            isCategoryTransitioning = false;
            oldTriggers.clear();
            oldTriggerAnimations.clear();
            categoryTransitionProgress = 1f;
        }
    }

    private void updateTriggerAnimations(List<TriggerDto> displayTriggers) {
        long currentTime = System.currentTimeMillis();
        for (TriggerDto trigger : displayTriggers) {
            Long startTime = triggerAnimStartTimes.get(trigger);
            if (startTime == null) continue;

            float elapsed = currentTime - startTime;
            float progress = Math.min(1f, Math.max(0f, elapsed / TRIGGER_ANIM_DURATION));
            progress = easeOutCubic(progress);
            triggerAnimations.put(trigger, progress);
        }
    }

    private void updateStateAnimations(List<TriggerDto> displayTriggers) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((currentTime - lastStateUpdateTime) / 1000f, 0.1f);
        lastStateUpdateTime = currentTime;

        for (TriggerDto trigger : displayTriggers) {
            float currentAnim = stateAnimations.getOrDefault(trigger, trigger.isActive ? 1f : 0f);
            float targetAnim = trigger.isActive ? 1f : 0f;
            stateAnimations.put(trigger, animateTowards(currentAnim, targetAnim, STATE_ANIM_SPEED, deltaTime));
        }
    }

    private void updateSelectedIconAnimations(List<TriggerDto> displayTriggers, TriggerDto selectedTrigger) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((currentTime - lastIconUpdateTime) / 1000f, 0.1f);
        lastIconUpdateTime = currentTime;

        for (TriggerDto trigger : displayTriggers) {
            float currentAnim = selectedIconAnimations.getOrDefault(trigger, 0f);
            float targetAnim = (trigger == selectedTrigger) ? 1f : 0f;
            selectedIconAnimations.put(trigger, animateTowards(currentAnim, targetAnim, ICON_ANIM_SPEED, deltaTime));
        }
    }

    private void updateHoverAnimations(List<TriggerDto> displayTriggers, float mouseX, float mouseY,
                                       float listX, float listY, float listWidth, float listHeight, float scrollOffset) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((currentTime - lastHoverUpdateTime) / 1000f, 0.1f);
        lastHoverUpdateTime = currentTime;

        selectedPulseAnimation += deltaTime * PULSE_SPEED;
        if (selectedPulseAnimation > Math.PI * 2) {
            selectedPulseAnimation -= (float) (Math.PI * 2);
        }

        float topInset = CORNER_INSET;
        float bottomInset = CORNER_INSET;
        float startY = listY + topInset + 2f + scrollOffset;
        float itemHeight = TRIGGER_ITEM_HEIGHT;

        float visibleTop = listY + topInset;
        float visibleBottom = listY + listHeight - bottomInset;

        for (int i = 0; i < displayTriggers.size(); i++) {
            TriggerDto trigger = displayTriggers.get(i);
            float triggerY = startY + i * (itemHeight + 2);

            boolean isInVisibleArea = triggerY + itemHeight >= visibleTop && triggerY <= visibleBottom;

            boolean isHovered = !isCategoryTransitioning &&
                    isInVisibleArea &&
                    mouseX >= listX + 3 && mouseX <= listX + listWidth - 3 &&
                    mouseY >= Math.max(triggerY, visibleTop) && mouseY <= Math.min(triggerY + itemHeight, visibleBottom) &&
                    mouseY >= triggerY && mouseY <= triggerY + itemHeight;

            float currentHover = hoverAnimations.getOrDefault(trigger, 0f);
            float targetHover = isHovered ? 1f : 0f;
            hoverAnimations.put(trigger, animateTowards(currentHover, targetHover, HOVER_ANIM_SPEED, deltaTime));
        }
    }

    private void updateHighlightAnimation() {
        if (highlightedTrigger == null) return;

        long elapsed = System.currentTimeMillis() - highlightStartTime;

        if (elapsed >= HIGHLIGHT_DURATION) {
            long fadeElapsed = elapsed - (long) HIGHLIGHT_DURATION;
            float fadeProgress = fadeElapsed / 500f;

            if (fadeProgress >= 1f) {
                highlightedTrigger = null;
                highlightAnimation = 0f;
            } else {
                highlightAnimation = 1f - fadeProgress;
            }
        } else {
            highlightAnimation = 1f;
        }
    }

    public void updatePositionAnimations(List<TriggerDto> displayTriggers) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((currentTime - lastStateUpdateTime) / 1000f, 0.1f);

        for (TriggerDto trigger : displayTriggers) {
            float currentPosAnim = positionAnimations.getOrDefault(trigger, 1f);
            if (currentPosAnim < 1f) {
                positionAnimations.put(trigger, Math.min(1f, currentPosAnim + POSITION_ANIM_SPEED * deltaTime));
            }

            float currentAlphaAnim = triggerAlphaAnimations.getOrDefault(trigger, 1f);
            if (currentAlphaAnim < 1f) {
                triggerAlphaAnimations.put(trigger, Math.min(1f, currentAlphaAnim + POSITION_ANIM_SPEED * deltaTime));
            }
        }
    }

    public void startHighlight(TriggerDto trigger) {
        highlightedTrigger = trigger;
        highlightStartTime = System.currentTimeMillis();
        highlightAnimation = 1f;
    }

    public void setScrollTarget(TriggerDto trigger) {
        scrollToTrigger = true;
        scrollTargetTrigger = trigger;
    }

    public boolean shouldScrollToTrigger() {
        return scrollToTrigger;
    }

    public void clearScrollTarget() {
        scrollToTrigger = false;
        scrollTargetTrigger = null;
    }

    private float animateTowards(float current, float target, float speed, float deltaTime) {
        float diff = target - current;
        if (Math.abs(diff) < 0.001f) return target;
        return current + diff * speed * deltaTime;
    }

    private float easeOutCubic(float x) {
        return 1f - (float) Math.pow(1 - x, 3);
    }

    public float easeInCubic(float x) {
        return x * x * x;
    }

    public float easeOutQuart(float x) {
        return 1f - (float) Math.pow(1 - x, 4);
    }

    public float getCategorySlideDistance() {
        return 40f;
    }
}
