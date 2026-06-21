package net.bummy1337.daintegrate.fabric.gui.clickgui.impl.background.search;

import net.bummy1337.daintegrate.configurations.TriggerDto;

import java.util.*;

public class SearchHandler {
    private boolean searchActive = false;
    private String searchText = "";
    private int searchCursorPosition = 0;
    private boolean hasSearchSelection = false;
    private int searchSelectionStart = 0;
    private int searchSelectionEnd = 0;
    private float searchFocusAnimation = 0f;
    private float searchSelectionAnimation = 0f;
    private float searchCursorBlink = 0f;
    private float normalPanelAlpha = 1f;
    private float searchPanelAlpha = 0f;

    private List<TriggerDto> allTriggers = new ArrayList<>();
    private List<TriggerDto> searchResults = new ArrayList<>();
    private TriggerDto selectedSearchTrigger = null;
    private int hoveredSearchIndex = -1;
    private float searchScrollOffset = 0f;
    private float searchResultHeight = 22f;
    private final Map<TriggerDto, Float> searchResultAnimations = new HashMap<>();

    public boolean isSearchActive() { return searchActive; }
    public void setSearchActive(boolean active) { this.searchActive = active; }

    public String getSearchText() { return searchText; }
    public void setSearchText(String text) { this.searchText = text; }

    public int getSearchCursorPosition() { return searchCursorPosition; }
    public void setSearchCursorPosition(int pos) { this.searchCursorPosition = pos; }

    public boolean hasSearchSelection() { return hasSearchSelection; }
    public void setHasSearchSelection(boolean has) { this.hasSearchSelection = has; }

    public int getSearchSelectionStart() { return searchSelectionStart; }
    public void setSearchSelectionStart(int start) { this.searchSelectionStart = start; }

    public int getSearchSelectionEnd() { return searchSelectionEnd; }
    public void setSearchSelectionEnd(int end) { this.searchSelectionEnd = end; }

    public float getSearchFocusAnimation() { return searchFocusAnimation; }
    public void setSearchFocusAnimation(float anim) { this.searchFocusAnimation = anim; }

    public float getSearchSelectionAnimation() { return searchSelectionAnimation; }
    public void setSearchSelectionAnimation(float anim) { this.searchSelectionAnimation = anim; }

    public float getSearchCursorBlink() { return searchCursorBlink; }
    public void setSearchCursorBlink(float blink) { this.searchCursorBlink = blink; }

    public float getNormalPanelAlpha() { return normalPanelAlpha; }
    public void setNormalPanelAlpha(float alpha) { this.normalPanelAlpha = alpha; }

    public float getSearchPanelAlpha() { return searchPanelAlpha; }
    public void setSearchPanelAlpha(float alpha) { this.searchPanelAlpha = alpha; }

    public List<TriggerDto> getAllTriggers() { return allTriggers; }
    public void setAllTriggers(List<TriggerDto> triggers) { this.allTriggers = triggers; }

    public List<TriggerDto> getSearchResults() { return searchResults; }
    public void setSearchResults(List<TriggerDto> results) { this.searchResults = results; }

    public TriggerDto getSelectedSearchTrigger() { return selectedSearchTrigger; }
    public void setSelectedSearchTrigger(TriggerDto trigger) { this.selectedSearchTrigger = trigger; }

    public int getHoveredSearchIndex() { return hoveredSearchIndex; }
    public void setHoveredSearchIndex(int index) { this.hoveredSearchIndex = index; }

    public float getSearchScrollOffset() { return searchScrollOffset; }
    public void setSearchScrollOffset(float offset) { this.searchScrollOffset = offset; }

    public float getSearchResultHeight() { return searchResultHeight; }
    public void setSearchResultHeight(float height) { this.searchResultHeight = height; }

    public Map<TriggerDto, Float> getSearchResultAnimations() { return searchResultAnimations; }

    public void updateAnimations(float deltaTime) {
        float focusTarget = searchActive ? 1f : 0f;
        searchFocusAnimation = animateTowards(searchFocusAnimation, focusTarget, 8f, deltaTime);

        float normalTarget = searchActive ? 0f : 1f;
        normalPanelAlpha = animateTowards(normalPanelAlpha, normalTarget, 8f, deltaTime);

        float panelTarget = searchActive ? 1f : 0f;
        searchPanelAlpha = animateTowards(searchPanelAlpha, panelTarget, 8f, deltaTime);

        searchCursorBlink += deltaTime;

        float selectionTarget = hasSearchSelection ? 1f : 0f;
        searchSelectionAnimation = animateTowards(searchSelectionAnimation, selectionTarget, 10f, deltaTime);
    }

    public boolean handleSearchChar(char chr) {
        if (!searchActive) return false;
        if (chr == '\b') {
            if (!searchText.isEmpty() && searchCursorPosition > 0) {
                searchText = searchText.substring(0, searchCursorPosition - 1) + searchText.substring(searchCursorPosition);
                searchCursorPosition--;
                updateSearchResults();
            }
            return true;
        }
        if (chr >= 32 && chr < 127) {
            searchText = searchText.substring(0, searchCursorPosition) + chr + searchText.substring(searchCursorPosition);
            searchCursorPosition++;
            updateSearchResults();
            return true;
        }
        return false;
    }

    public boolean handleSearchKey(int keyCode) {
        if (!searchActive) return false;
        if (keyCode == 259) {
            if (!searchText.isEmpty() && searchCursorPosition > 0) {
                searchText = searchText.substring(0, searchCursorPosition - 1) + searchText.substring(searchCursorPosition);
                searchCursorPosition--;
                updateSearchResults();
            }
            return true;
        }
        return false;
    }

    public void handleSearchScroll(double vertical, float panelHeight) {
        float maxScroll = Math.max(0, searchResults.size() * (searchResultHeight + 2) - panelHeight + 10);
        searchScrollOffset = (float) Math.max(-maxScroll, Math.min(0, searchScrollOffset + vertical * 25));
    }

    private void updateSearchResults() {
        searchResults.clear();
        searchResultAnimations.clear();
        if (searchText.isEmpty()) return;

        String query = searchText.toLowerCase();
        for (TriggerDto trigger : allTriggers) {
            if (trigger.name.toLowerCase().contains(query) ||
                    (trigger.description != null && trigger.description.toLowerCase().contains(query))) {
                searchResults.add(trigger);
                searchResultAnimations.put(trigger, 0f);
            }
        }

        for (int i = 0; i < searchResults.size(); i++) {
            TriggerDto t = searchResults.get(i);
            searchResultAnimations.put(t, 0f);
        }
    }

    private float animateTowards(float current, float target, float speed, float deltaTime) {
        float diff = target - current;
        if (Math.abs(diff) < 0.001f) return target;
        return current + diff * speed * deltaTime;
    }
}
