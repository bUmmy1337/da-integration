package net.bummy1337.daintegrate.fabric.gui.clickgui.impl.background.search;

import net.bummy1337.daintegrate.configurations.TriggerDto;
import net.bummy1337.daintegrate.fabric.gui.Theme;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.RenderHelper;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

public class SearchRenderer {

    private final SearchHandler searchHandler;

    public SearchRenderer(SearchHandler searchHandler) {
        this.searchHandler = searchHandler;
    }

    public void render(GuiGraphicsExtractor g, float bgX, float bgY, float bgWidth, float bgHeight,
                       float mouseX, float mouseY, int guiScale, float alphaMultiplier) {

        if (searchHandler.getSearchPanelAlpha() <= 0.01f) return;

        float panelX = bgX + 92f;
        float panelY = bgY + 38f;
        float panelW = bgWidth - 100f;
        float panelH = bgHeight - 46f;

        float resultAlpha = searchHandler.getSearchPanelAlpha() * alphaMultiplier;

        renderPanelBackground(g, panelX, panelY, panelW, panelH, resultAlpha);

        List<TriggerDto> results = searchHandler.getSearchResults();
        if (results.isEmpty()) {
            renderEmptyState(g, panelX, panelY, panelW, panelH, resultAlpha);
            return;
        }

        RenderHelper.enableScissor(g, panelX + 3, panelY + 3, panelW - 6, panelH - 6);
        renderResults(g, panelX, panelY, panelW, panelH, mouseX, mouseY, resultAlpha);
        RenderHelper.disableScissor(g);

        renderScrollIndicators(g, panelX, panelY, panelW, panelH, resultAlpha);
    }

    private void renderPanelBackground(GuiGraphicsExtractor g, float panelX, float panelY, float panelW, float panelH, float resultAlpha) {
        int panelBg = Theme.withAlpha(Theme.BG_PANEL, resultAlpha);
        int outlineColor = Theme.withAlpha(Theme.BORDER, resultAlpha);

        RenderHelper.roundedRect(g, panelX, panelY, panelW, panelH, 8f, panelBg);
        RenderHelper.roundedOutline(g, panelX, panelY, panelW, panelH, 8f, outlineColor);
    }

    private void renderEmptyState(GuiGraphicsExtractor g, float panelX, float panelY, float panelW, float panelH, float resultAlpha) {
        String noResults = searchHandler.getSearchText().isEmpty() ? "Start typing to search..." : "No triggers found";
        float textWidth = RenderHelper.textWidth(noResults, 6f);
        float centerX = panelX + (panelW - textWidth) / 2f;
        float centerY = panelY + (panelH - 6f) / 2f;
        int textColor = Theme.withAlpha(Theme.TEXT_MUTED, (int) (150 * resultAlpha));
        RenderHelper.text(g, noResults, centerX, centerY, 6f, textColor);
    }

    private void renderResults(GuiGraphicsExtractor g, float panelX, float panelY, float panelW, float panelH,
                               float mouseX, float mouseY, float resultAlpha) {

        List<TriggerDto> results = searchHandler.getSearchResults();
        float startY = panelY + 5 + searchHandler.getSearchScrollOffset();
        float resultHeight = searchHandler.getSearchResultHeight();

        int newHoveredIndex = -1;

        for (int i = 0; i < results.size(); i++) {
            TriggerDto trigger = results.get(i);
            float itemY = startY + i * (resultHeight + 2);

            if (itemY + resultHeight < panelY || itemY > panelY + panelH) continue;

            float itemAnim = searchHandler.getSearchResultAnimations().getOrDefault(trigger, 0f);
            float itemAlpha = itemAnim * resultAlpha;

            if (itemAlpha <= 0.01f) continue;

            float itemOffsetX = (1f - itemAnim) * 20f;

            boolean hovered = mouseX >= panelX + 5 && mouseX <= panelX + panelW - 5 &&
                    mouseY >= itemY && mouseY <= itemY + resultHeight;

            if (hovered) {
                newHoveredIndex = i;
            }

            boolean selected = trigger == searchHandler.getSelectedSearchTrigger();

            renderResultItem(g, trigger, panelX, itemY, panelW, resultHeight,
                    itemOffsetX, itemAlpha, hovered, selected);
        }

        searchHandler.setHoveredSearchIndex(newHoveredIndex);
    }

    private void renderResultItem(GuiGraphicsExtractor g, TriggerDto trigger, float panelX, float itemY, float panelW,
                                  float resultHeight, float itemOffsetX, float itemAlpha,
                                  boolean hovered, boolean selected) {

        int bg;
        if (selected) {
            bg = Theme.withAlpha(Theme.BG_ENTRY_HOVER, (int) (60 * itemAlpha));
        } else if (hovered) {
            bg = Theme.withAlpha(Theme.BG_PANEL_HOVER, (int) (40 * itemAlpha));
        } else {
            bg = Theme.withAlpha(Theme.BG_ENTRY, (int) (25 * itemAlpha));
        }

        float itemX = panelX + 5 + itemOffsetX;
        float itemW = panelW - 10;

        RenderHelper.roundedRect(g, itemX, itemY, itemW, resultHeight, 4f, bg);

        if (selected) {
            RenderHelper.outline(g, itemX, itemY, itemW, resultHeight, 0.5f,
                    Theme.withAlpha(Theme.ACCENT, (int) (100 * itemAlpha)));
        }

        int textColor = trigger.isActive
                ? Theme.withAlpha(Theme.TEXT_PRIMARY, (int) (255 * itemAlpha))
                : Theme.withAlpha(Theme.TEXT_SECONDARY, (int) (200 * itemAlpha));

        RenderHelper.text(g, trigger.name, itemX + 5, itemY + 3, 6f, textColor);

        int categoryColor = Theme.withAlpha(Theme.TEXT_MUTED, (int) (180 * itemAlpha));
        RenderHelper.text(g, trigger.description != null ? trigger.description : "", itemX + 5, itemY + 11, 4f, categoryColor);

        if (trigger.isActive) {
            float indicatorX = itemX + itemW - 10;
            float indicatorY = itemY + resultHeight / 2 - 2;
            int greenColor = Theme.withAlpha(Theme.GREEN, (int) (200 * itemAlpha));
            RenderHelper.roundedRect(g, indicatorX, indicatorY, 4, 4, 2f, greenColor);
        }
    }

    private void renderScrollIndicators(GuiGraphicsExtractor g, float panelX, float panelY, float panelW, float panelH, float resultAlpha) {
        List<TriggerDto> results = searchHandler.getSearchResults();
        float resultHeight = searchHandler.getSearchResultHeight();
        float maxScroll = Math.max(0, results.size() * (resultHeight + 2) - panelH + 10);

        if (maxScroll > 0) {
            if (searchHandler.getSearchScrollOffset() < -0.5f) {
                for (int i = 0; i < 10; i++) {
                    float fadeAlpha = 60 * resultAlpha * (1f - i / 10f);
                    int fadeColor = Theme.withAlpha(Theme.BG_DARK, (int) fadeAlpha);
                    RenderHelper.rect(g, panelX + 3, panelY + 3 + i, panelW - 6, 1, fadeColor);
                }
            }
            if (searchHandler.getSearchScrollOffset() > -maxScroll + 0.5f) {
                for (int i = 0; i < 10; i++) {
                    float fadeAlpha = 60 * resultAlpha * (i / 10f);
                    int fadeColor = Theme.withAlpha(Theme.BG_DARK, (int) fadeAlpha);
                    RenderHelper.rect(g, panelX + 3, panelY + panelH - 13 + i, panelW - 6, 1, fadeColor);
                }
            }
        }
    }

    public TriggerDto getTriggerAtPosition(double mouseX, double mouseY, float bgX, float bgY,
                                           float bgWidth, float bgHeight, SearchHandler handler) {

        if (!handler.isSearchActive() || handler.getSearchResults().isEmpty()) return null;

        float panelX = bgX + 92f;
        float panelY = bgY + 38f;
        float panelW = bgWidth - 100f;
        float panelH = bgHeight - 46f;

        if (mouseX < panelX + 5 || mouseX > panelX + panelW - 5 ||
                mouseY < panelY || mouseY > panelY + panelH) return null;

        float startY = panelY + 5 + handler.getSearchScrollOffset();
        float resultHeight = handler.getSearchResultHeight();

        List<TriggerDto> results = handler.getSearchResults();
        for (int i = 0; i < results.size(); i++) {
            float itemY = startY + i * (resultHeight + 2);

            if (mouseY >= itemY && mouseY <= itemY + resultHeight) {
                return results.get(i);
            }
        }

        return null;
    }
}
