package net.bummy1337.daintegrate.fabric.imgui;

import com.mojang.blaze3d.platform.NativeImage;
import imgui.ImDrawData;
import net.bummy1337.daintegrate.Constants;
import net.bummy1337.daintegrate.fabric.mixin.GuiGraphicsExtractorAccessor;
import net.bummy1337.daintegrate.ui.DrawBatches;
import net.bummy1337.daintegrate.ui.ImGuiHost;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

/**
 * Uploads ImGui's font atlas as a Minecraft texture and turns ImGui draw data into GUI elements.
 */
public class MinecraftImGuiRenderer implements ImGuiHost.FontTextureUploader {
    private final Map<Long, DynamicTexture> textures = new HashMap<>();
    private final Map<Long, Identifier> identifiers = new HashMap<>();
    private long nextId = 1;

    @Override
    public long upload(ByteBuffer rgba, int width, int height) {
        var image = new NativeImage(width, height, false);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = (y * width + x) * 4;
                int r = rgba.get(i) & 0xFF;
                int g = rgba.get(i + 1) & 0xFF;
                int b = rgba.get(i + 2) & 0xFF;
                int a = rgba.get(i + 3) & 0xFF;
                image.setPixel(x, y, (a << 24) | (r << 16) | (g << 8) | b);
            }
        }
        long id = nextId++;
        var identifier = Identifier.fromNamespaceAndPath(Constants.ModId, "imgui/font_" + id);
        var texture = new DynamicTexture(identifier::toString, image);
        Minecraft.getInstance().getTextureManager().register(identifier, texture);
        textures.put(id, texture);
        identifiers.put(id, identifier);
        return id;
    }

    @Override
    public void release(long textureId) {
        var identifier = identifiers.remove(textureId);
        textures.remove(textureId);
        if (identifier != null)
            Minecraft.getInstance().getTextureManager().release(identifier);
    }

    /**
     * Submits everything ImGui drew this frame. ImGui works in window pixels,
     * the GUI renderer in scaled GUI units, hence the division by the GUI scale.
     */
    public void submit(GuiGraphicsExtractor graphics, ImDrawData drawData, float guiScale) {
        var renderState = ((GuiGraphicsExtractorAccessor) graphics).daintegrate$getGuiRenderState();
        for (var batch : DrawBatches.build(drawData, guiScale)) {
            var texture = textures.get(batch.textureId());
            if (texture == null)
                continue;
            var scissor = new ScreenRectangle(batch.clipLeft(), batch.clipTop(),
                    batch.clipRight() - batch.clipLeft(), batch.clipBottom() - batch.clipTop());
            var setup = TextureSetup.singleTexture(texture.getTextureView(), texture.getSampler());
            renderState.submitGuiElement(new ImGuiDrawState(batch, setup, scissor));
        }
    }
}
