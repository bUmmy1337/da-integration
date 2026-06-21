package net.bummy1337.daintegrate.fabric.gui.clickgui.impl.background;

import net.bummy1337.daintegrate.configurations.TriggerDto;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.background.render.AvatarRenderer;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.background.render.BackgroundRenderer;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.background.render.CategoryRenderer;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.background.render.HeaderRenderer;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.background.search.SearchHandler;
import net.bummy1337.daintegrate.fabric.gui.clickgui.impl.background.search.SearchRenderer;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.util.List;

public class BackgroundComponent {
    public static final int BG_WIDTH = 620;
    public static final int BG_HEIGHT = 360;

    private final BackgroundRenderer backgroundRenderer;
    private final CategoryRenderer categoryRenderer;
    private final HeaderRenderer headerRenderer;
    private final AvatarRenderer avatarRenderer;
    private final SearchHandler searchHandler;
    private final SearchRenderer searchRenderer;

    private String previousCategory = null;
    private String currentCategory = null;
    private float headerTransition = 1f;

    private static final float HEADER_SPEED = 3f;
    private long lastUpdateTime = System.currentTimeMillis();

    public BackgroundComponent() {
        this.backgroundRenderer = new BackgroundRenderer();
        this.categoryRenderer = new CategoryRenderer();
        this.headerRenderer = new HeaderRenderer();
        this.avatarRenderer = new AvatarRenderer();
        this.searchHandler = new SearchHandler();
        this.searchRenderer = new SearchRenderer(searchHandler);
    }

    public boolean isSearchActive() {
        return searchHandler.isSearchActive();
    }

    public float getSearchPanelAlpha() {
        return searchHandler.getSearchPanelAlpha();
    }

    public float getNormalPanelAlpha() {
        return searchHandler.getNormalPanelAlpha();
    }

    public void setSearchActive(boolean active) {
        searchHandler.setSearchActive(active);
    }

    public String getSearchText() {
        return searchHandler.getSearchText();
    }

    public List<TriggerDto> getSearchResults() {
        return searchHandler.getSearchResults();
    }

    public TriggerDto getSelectedSearchTrigger() {
        return searchHandler.getSelectedSearchTrigger();
    }

    public void updateAnimations(String selectedCategory, float delta) {
        long currentTime = System.currentTimeMillis();
        float deltaTime = Math.min((currentTime - lastUpdateTime) / 1000f, 0.1f);
        lastUpdateTime = currentTime;

        if (currentCategory != selectedCategory) {
            previousCategory = currentCategory;
            currentCategory = selectedCategory;
            headerTransition = 0f;
        }

        if (headerTransition < 1f) {
            headerTransition += HEADER_SPEED * deltaTime;
            if (headerTransition > 1f) {
                headerTransition = 1f;
            }
        }

        categoryRenderer.updateAnimations(selectedCategory, deltaTime);
        searchHandler.updateAnimations(deltaTime);
    }

    public void render(GuiGraphicsExtractor g, float bgX, float bgY, String selectedCategory, float delta, float alphaMultiplier) {
        updateAnimations(selectedCategory, delta);
        backgroundRenderer.render(g, bgX, bgY, BG_WIDTH, BG_HEIGHT, alphaMultiplier);
        avatarRenderer.render(g, bgX, bgY, alphaMultiplier);
    }

    public void renderCategoryPanel(GuiGraphicsExtractor g, float bgX, float bgY, float alphaMultiplier) {
        backgroundRenderer.renderCategoryPanel(g, bgX, bgY, BG_HEIGHT, alphaMultiplier);
    }

    public void renderHeader(GuiGraphicsExtractor g, float bgX, float bgY, String selectedCategory, float alphaMultiplier) {
        headerRenderer.render(g, bgX, bgY, BG_WIDTH, selectedCategory, previousCategory, currentCategory,
                headerTransition, searchHandler, alphaMultiplier);
    }

    public void renderCategoryNames(GuiGraphicsExtractor g, float bgX, float bgY, String selectedCategory, float alphaMultiplier) {
        categoryRenderer.render(g, bgX, bgY, selectedCategory, alphaMultiplier);
    }

    public void renderSearchResults(GuiGraphicsExtractor g, float bgX, float bgY, float mouseX, float mouseY, int guiScale, float alphaMultiplier) {
        searchRenderer.render(g, bgX, bgY, BG_WIDTH, BG_HEIGHT, mouseX, mouseY, guiScale, alphaMultiplier);
    }

    public boolean handleSearchChar(char chr) {
        return searchHandler.handleSearchChar(chr);
    }

    public boolean handleSearchKey(int keyCode) {
        return searchHandler.handleSearchKey(keyCode);
    }

    public void handleSearchScroll(double vertical, float panelHeight) {
        searchHandler.handleSearchScroll(vertical, panelHeight);
    }

    public boolean isSearchBoxHovered(double mouseX, double mouseY, float bgX, float bgY) {
        return headerRenderer.isSearchBoxHovered(mouseX, mouseY, bgX, bgY);
    }

    public TriggerDto getSearchTriggerAtPosition(double mouseX, double mouseY, float bgX, float bgY) {
        return searchRenderer.getTriggerAtPosition(mouseX, mouseY, bgX, bgY, BG_WIDTH, BG_HEIGHT, searchHandler);
    }

    public String getCategoryAtPosition(double mouseX, double mouseY, float bgX, float bgY) {
        return categoryRenderer.getCategoryAtPosition(mouseX, mouseY, bgX, bgY);
    }

    public SearchHandler getSearchHandler() {
        return searchHandler;
    }

    public SearchRenderer getSearchRenderer() {
        return searchRenderer;
    }
}
