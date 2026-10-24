package net.bummy1337.daintegrate.fabric.imgui;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.bummy1337.daintegrate.ui.DrawBatches;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.gui.render.TextureSetup;
import net.minecraft.client.gui.render.state.GuiElementRenderState;
import net.minecraft.client.renderer.RenderPipelines;
import org.joml.Matrix3x2f;

/**
 * One ImGui draw command as a Minecraft GUI element. Triangles are written as
 * degenerate quads (the last vertex repeated) because the GUI pipelines draw quads.
 * Going through the game's own GUI renderer keeps the menu working on both the
 * OpenGL and the Vulkan backend.
 */
public record ImGuiDrawState(
        DrawBatches.Batch batch,
        TextureSetup textureSetup,
        ScreenRectangle scissorArea
) implements GuiElementRenderState {
    private static final Matrix3x2f Identity = new Matrix3x2f();

    @Override
    public void buildVertices(VertexConsumer consumer) {
        int triangles = batch.vertexCount() / 3;
        for (int t = 0; t < triangles; t++) {
            int base = t * 3;
            vertex(consumer, base);
            vertex(consumer, base + 1);
            vertex(consumer, base + 2);
            vertex(consumer, base + 2);
        }
    }

    private void vertex(VertexConsumer consumer, int index) {
        var vertices = batch.vertices();
        int offset = index * DrawBatches.Batch.Stride;
        consumer.addVertexWith2DPose(Identity, vertices[offset], vertices[offset + 1])
                .setUv(vertices[offset + 2], vertices[offset + 3])
                .setColor(batch.colors()[index]);
    }

    @Override
    public RenderPipeline pipeline() {
        return RenderPipelines.GUI_TEXTURED;
    }

    @Override
    public ScreenRectangle bounds() {
        return scissorArea;
    }
}
