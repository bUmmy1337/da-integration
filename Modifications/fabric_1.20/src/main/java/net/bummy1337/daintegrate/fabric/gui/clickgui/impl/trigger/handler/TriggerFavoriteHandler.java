package net.bummy1337.daintegrate.fabric.gui.clickgui.impl.trigger.handler;

import net.bummy1337.daintegrate.configurations.TriggerDto;

import java.util.List;

public class TriggerFavoriteHandler {
    public void toggleFavorite(TriggerDto trigger, List<TriggerDto> displayTriggers,
                               TriggerAnimationHandler animationHandler) {
        if (trigger == null) return;
        int oldIndex = displayTriggers.indexOf(trigger);
        for (TriggerDto t : displayTriggers) {
            float posAnim = animationHandler.getPositionAnimations().getOrDefault(t, 1f);
            if (posAnim >= 0.99f) {
                animationHandler.getPositionAnimations().put(t, 0f);
            }
            if (!animationHandler.getTriggerAlphaAnimations().containsKey(t)) {
                animationHandler.getTriggerAlphaAnimations().put(t, 1f);
            }
        }
        animationHandler.getTriggerAlphaAnimations().put(trigger, 0f);
    }
}
