package net.bummy1337.daintegrate.fabric.gui.clickgui.impl.background.render;

import net.bummy1337.daintegrate.fabric.gui.Theme;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.RenderHelper;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.background.search.SearchHandler;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class HeaderRenderer {

    private static final float HEADER_SLIDE_DISTANCE = 8f;

    public void render(GuiGraphicsExtractor g, float bgX, float bgY, float bgWidth, String selectedCategory,
                       String previousCategory, String currentCategory, float headerTransition,
                       SearchHandler searchHandler, float alphaMultiplier) {
        renderHeaderPanel(g, bgX, bgY, bgWidth, alphaMultiplier);
        renderSearchBox(g, bgX, bgY, bgWidth, searchHandler, alphaMultiplier);
        renderCategoryLabel(g, bgX, bgY, previousCategory, currentCategory, headerTransition, searchHandler, alphaMultiplier);
    }

    private void renderHeaderPanel(GuiGraphicsExtractor g, float bgX, float bgY, float bgWidth, float alphaMultiplier) {
        float x = bgX + 92f;
        float y = bgY + 7.5f;
        float w = bgWidth - 100f;
        float h = 25;

        RenderHelper.roundedRect(g, x, y, w, h, 7f, Theme.withAlpha(Theme.BG_PANEL, alphaMultiplier));
        RenderHelper.roundedOutline(g, x, y, w, h, 7f, Theme.withAlpha(Theme.BORDER, alphaMultiplier));
    }

    private void renderSearchBox(GuiGraphicsExtractor g, float bgX, float bgY, float bgWidth, SearchHandler searchHandler, float alphaMultiplier) {
        float searchBoxX = bgX + bgWidth - 98f;
        float searchBoxY = bgY + 12f;
        float searchBoxW = 88f;
        float searchBoxH = 16f;
        float textAreaX = searchBoxX + 7;

        int bgAlpha = (int) ((25 + searchHandler.getSearchFocusAnimation() * 15) * alphaMultiplier);
        int outlineColor = searchHandler.isSearchActive()
                ? Theme.withAlpha(Theme.TEXT_SECONDARY, alphaMultiplier)
                : Theme.withAlpha(Theme.BORDER, alphaMultiplier);

        RenderHelper.roundedRect(g, searchBoxX, searchBoxY, searchBoxW, searchBoxH, 5f, Theme.withAlpha(Theme.BG_INPUT, bgAlpha / 255f));
        RenderHelper.roundedOutline(g, searchBoxX, searchBoxY, searchBoxW, searchBoxH, 5f, outlineColor);

        if (searchHandler.isSearchActive() && !searchHandler.getSearchText().isEmpty()) {
            renderSearchText(g, searchBoxX, searchBoxY, searchBoxW, searchBoxH, textAreaX, searchHandler, alphaMultiplier);
        } else if (searchHandler.isSearchActive()) {
            renderSearchPlaceholder(g, searchBoxX, searchBoxY, searchBoxH, textAreaX, searchHandler, alphaMultiplier, true);
        } else {
            RenderHelper.text(g, "Search...", textAreaX, searchBoxY + 4f, 5, Theme.withAlpha(Theme.TEXT_MUTED, alphaMultiplier));
        }

        RenderHelper.rect(g, searchBoxX + searchBoxW - 18, searchBoxY + 4f, 1, searchBoxH - 8, Theme.withAlpha(Theme.TEXT_MUTED, alphaMultiplier));
    }

    private void renderSearchText(GuiGraphicsExtractor g, float searchBoxX, float searchBoxY, float searchBoxW, float searchBoxH,
                                  float textAreaX, SearchHandler searchHandler, float alphaMultiplier) {
        RenderHelper.enableScissor(g, searchBoxX + 3, searchBoxY, searchBoxW - 20, searchBoxH);

        if (searchHandler.hasSearchSelection() && searchHandler.getSearchSelectionAnimation() > 0.01f) {
            renderSearchSelection(g, textAreaX, searchBoxY, searchBoxH, searchHandler, alphaMultiplier);
        }

        RenderHelper.text(g, searchHandler.getSearchText(), textAreaX, searchBoxY + 5f, 5,
                Theme.withAlpha(Theme.TEXT_PRIMARY, alphaMultiplier));
        RenderHelper.disableScissor(g);

        if (!searchHandler.hasSearchSelection()) {
            renderSearchCursor(g, textAreaX, searchBoxY, searchBoxH, searchHandler, alphaMultiplier);
        }
    }

    private void renderSearchSelection(GuiGraphicsExtractor g, float textAreaX, float searchBoxY, float searchBoxH,
                                       SearchHandler searchHandler, float alphaMultiplier) {
        int start = searchHandler.getSearchSelectionStart();
        int end = searchHandler.getSearchSelectionEnd();
        String beforeSelection = searchHandler.getSearchText().substring(0, start);
        String selection = searchHandler.getSearchText().substring(start, end);

        float selectionX = textAreaX + RenderHelper.textWidth(beforeSelection, 5);
        float selectionWidth = RenderHelper.textWidth(selection, 5);

        int selAlpha = (int) (100 * searchHandler.getSearchSelectionAnimation() * alphaMultiplier);
        RenderHelper.rect(g, selectionX, searchBoxY + 2, selectionWidth, searchBoxH - 4,
                Theme.withAlpha(Theme.ACCENT, selAlpha));
    }

    private void renderSearchCursor(GuiGraphicsExtractor g, float textAreaX, float searchBoxY, float searchBoxH,
                                    SearchHandler searchHandler, float alphaMultiplier) {
        float cursorAlpha = (float) (Math.sin(searchHandler.getSearchCursorBlink() * Math.PI * 2) * 0.5 + 0.5);
        if (cursorAlpha > 0.3f) {
            String beforeCursor = searchHandler.getSearchText().substring(0, searchHandler.getSearchCursorPosition());
            float cursorX = textAreaX + RenderHelper.textWidth(beforeCursor, 5);
            int cursorAlphaInt = (int) (255 * cursorAlpha * alphaMultiplier);
            RenderHelper.rect(g, cursorX, searchBoxY + 3, 0.5f, searchBoxH - 6,
                    Theme.withAlpha(Theme.TEXT_SECONDARY, cursorAlphaInt));
        }
    }

    private void renderSearchPlaceholder(GuiGraphicsExtractor g, float searchBoxX, float searchBoxY, float searchBoxH,
                                         float textAreaX, SearchHandler searchHandler, float alphaMultiplier, boolean showCursor) {
        RenderHelper.text(g, "Type to search...", textAreaX, searchBoxY + 5f, 5,
                Theme.withAlpha(Theme.TEXT_MUTED, alphaMultiplier));

        if (showCursor) {
            float cursorAlpha = (float) (Math.sin(searchHandler.getSearchCursorBlink() * Math.PI * 2) * 0.5 + 0.5);
            if (cursorAlpha > 0.3f) {
                int cursorAlphaInt = (int) (255 * cursorAlpha * alphaMultiplier);
                RenderHelper.rect(g, textAreaX, searchBoxY + 3, 0.5f, searchBoxH - 6,
                        Theme.withAlpha(Theme.TEXT_SECONDARY, cursorAlphaInt));
            }
        }
    }

    private void renderCategoryLabel(GuiGraphicsExtractor g, float bgX, float bgY, String previousCategory,
                                     String currentCategory, float headerTransition, SearchHandler searchHandler,
                                     float alphaMultiplier) {
        float baseX = bgX + 100f;
        float baseY = bgY + 15f;

        float categoryAlpha = searchHandler.getNormalPanelAlpha() * alphaMultiplier;
        if (categoryAlpha > 0.01f) {
            float eased = easeOutQuart(headerTransition);

            if (previousCategory != null && headerTransition < 1f) {
                float oldAlpha = (1f - eased) * categoryAlpha;
                float oldOffsetY = eased * HEADER_SLIDE_DISTANCE;

                int oldAlphaInt = (int) (128 * oldAlpha);
                if (oldAlphaInt > 0) {
                    RenderHelper.text(g, previousCategory, baseX, baseY + oldOffsetY, 7,
                            Theme.withAlpha(Theme.TEXT_MUTED, oldAlphaInt));
                }
            }

            if (currentCategory != null) {
                float newAlpha = eased * categoryAlpha;
                float newOffsetY = (1f - eased) * -HEADER_SLIDE_DISTANCE;

                int newAlphaInt = (int) (128 * newAlpha);
                if (newAlphaInt > 0) {
                    RenderHelper.text(g, currentCategory, baseX, baseY + newOffsetY, 7,
                            Theme.withAlpha(Theme.TEXT_MUTED, newAlphaInt));
                }
            }
        }

        float searchLabelAlpha = searchHandler.getSearchPanelAlpha() * alphaMultiplier;
        if (searchLabelAlpha > 0.01f) {
            int searchLabelAlphaInt = (int) (180 * searchLabelAlpha);
            if (searchLabelAlphaInt > 0) {
                String searchLabel = "Search Results";
                String searchText = searchHandler.getSearchText();
                if (!searchText.isEmpty()) {
                    searchLabel = "Results for \"" + (searchText.length() > 12 ? searchText.substring(0, 12) + "..." : searchText) + "\"";
                }
                RenderHelper.text(g, searchLabel, baseX, baseY, 7,
                        Theme.withAlpha(Theme.TEXT_SECONDARY, searchLabelAlphaInt));
            }
        }
    }

    private float easeOutQuart(float x) {
        return 1f - (float) Math.pow(1 - x, 4);
    }

    public boolean isSearchBoxHovered(double mouseX, double mouseY, float bgX, float bgY) {
        float searchBoxX = bgX + 620f - 98f;
        float searchBoxY = bgY + 12f;
        float searchBoxW = 88f;
        float searchBoxH = 16f;

        return mouseX >= searchBoxX && mouseX <= searchBoxX + searchBoxW &&
                mouseY >= searchBoxY && mouseY <= searchBoxY + searchBoxH;
    }
}
