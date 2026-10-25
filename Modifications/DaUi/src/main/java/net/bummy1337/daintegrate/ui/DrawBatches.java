package net.bummy1337.daintegrate.ui;

import imgui.ImDrawData;
import imgui.ImVec4;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

/**
 * Flattens ImGui draw data into self-contained triangle batches in "GUI units"
 * (window pixels divided by a scale), with ARGB colours and integer clip rectangles.
 * Renderers that cannot use ImGui's own backends (Minecraft's GUI renderer) draw these.
 */
public final class DrawBatches {
    /** Triangles sharing one texture and clip rectangle. Vertices are x, y, u, v. */
    public record Batch(long textureId, int clipLeft, int clipTop, int clipRight, int clipBottom,
                        float[] vertices, int[] colors) {
        public static final int Stride = 4;

        public int vertexCount() {
            return colors.length;
        }
    }

    private DrawBatches() {
    }

    public static List<Batch> build(ImDrawData drawData, float scale) {
        var batches = new ArrayList<Batch>();
        if (drawData == null || drawData.getCmdListsCount() == 0)
            return batches;

        float offsetX = drawData.getDisplayPosX();
        float offsetY = drawData.getDisplayPosY();
        int vertexSize = ImDrawData.sizeOfImDrawVert();
        int indexSize = ImDrawData.sizeOfImDrawIdx();
        var clip = new ImVec4();
        // The game rejects a scissor that leaves its render area, and ImGui's clip rectangles can
        // (the dimmed backdrop of a modal popup does), which crashed the game
        int maxX = (int) Math.floor(drawData.getDisplaySizeX() / scale);
        int maxY = (int) Math.floor(drawData.getDisplaySizeY() / scale);

        for (int list = 0; list < drawData.getCmdListsCount(); list++) {
            // Both getters reuse one native buffer, so the vertices must be copied before reading indices
            var vertexData = copy(drawData.getCmdListVtxBufferData(list));
            var indexData = copy(drawData.getCmdListIdxBufferData(list));

            for (int cmd = 0; cmd < drawData.getCmdListCmdBufferSize(list); cmd++) {
                int elements = drawData.getCmdListCmdBufferElemCount(list, cmd);
                if (elements == 0)
                    continue;

                drawData.getCmdListCmdBufferClipRect(clip, list, cmd);
                int left = clamp((int) Math.floor((clip.x - offsetX) / scale), maxX);
                int top = clamp((int) Math.floor((clip.y - offsetY) / scale), maxY);
                int right = clamp((int) Math.ceil((clip.z - offsetX) / scale), maxX);
                int bottom = clamp((int) Math.ceil((clip.w - offsetY) / scale), maxY);
                if (right <= left || bottom <= top)
                    continue;

                int vertexOffset = drawData.getCmdListCmdBufferVtxOffset(list, cmd);
                int indexOffset = drawData.getCmdListCmdBufferIdxOffset(list, cmd);
                var vertices = new float[elements * Batch.Stride];
                var colors = new int[elements];
                for (int e = 0; e < elements; e++) {
                    int position = (indexOffset + e) * indexSize;
                    int index = indexSize == 2 ? indexData.getShort(position) & 0xFFFF : indexData.getInt(position);
                    int v = (vertexOffset + index) * vertexSize;
                    vertices[e * 4] = (vertexData.getFloat(v) - offsetX) / scale;
                    vertices[e * 4 + 1] = (vertexData.getFloat(v + 4) - offsetY) / scale;
                    vertices[e * 4 + 2] = vertexData.getFloat(v + 8);
                    vertices[e * 4 + 3] = vertexData.getFloat(v + 12);
                    int abgr = vertexData.getInt(v + 16);
                    colors[e] = (abgr & 0xFF00FF00) | ((abgr & 0xFF) << 16) | ((abgr >> 16) & 0xFF);
                }
                faceFront(vertices, colors);
                batches.add(new Batch(drawData.getCmdListCmdBufferTextureId(list, cmd), left, top, right, bottom, vertices, colors));
            }
        }
        return batches;
    }

    static int clamp(int value, int max) {
        return Math.max(0, Math.min(max, value));
    }

    /**
     * ImGui does not care about winding and emits most shapes clockwise, but the game's GUI
     * pipelines cull back faces, which made every fill and every glyph disappear. Turns each
     * triangle counter-clockwise on screen (y down), like the game's own GUI quads.
     */
    static void faceFront(float[] vertices, int[] colors) {
        int stride = Batch.Stride;
        for (int t = 0; t + 2 < colors.length; t += 3) {
            int a = t * stride;
            int b = a + stride;
            int c = b + stride;
            float cross = (vertices[b] - vertices[a]) * (vertices[c + 1] - vertices[a + 1])
                    - (vertices[b + 1] - vertices[a + 1]) * (vertices[c] - vertices[a]);
            if (cross <= 0)
                continue;
            for (int i = 0; i < stride; i++) {
                float swap = vertices[b + i];
                vertices[b + i] = vertices[c + i];
                vertices[c + i] = swap;
            }
            int swap = colors[t + 1];
            colors[t + 1] = colors[t + 2];
            colors[t + 2] = swap;
        }
    }

    private static ByteBuffer copy(ByteBuffer source) {
        var copy = ByteBuffer.allocate(source.remaining()).order(ByteOrder.nativeOrder());
        copy.put(source.duplicate());
        copy.flip();
        return copy;
    }
}
