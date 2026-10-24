package net.bummy1337.daintegrate.ui.preview;

import net.bummy1337.daintegrate.ui.DrawBatches;
import org.lwjgl.BufferUtils;

import java.util.List;

import static org.lwjgl.opengl.GL33.*;

/**
 * Draws {@link DrawBatches} the way the in-game renderer does: each triangle as a quad with
 * its last vertex repeated, positions in GUI units, clip rectangles rounded to whole GUI units.
 * Used by the preview's {@code --game-path} mode to check that conversion without Minecraft.
 */
class BatchRenderer {
    private final int program;
    private final int vao;
    private final int vbo;
    private final int ebo;
    private final int projection;

    BatchRenderer() {
        var vertex = """
                #version 330 core
                layout(location = 0) in vec2 Position;
                layout(location = 1) in vec2 UV;
                layout(location = 2) in vec4 Color;
                uniform vec2 GuiSize;
                out vec2 uv;
                out vec4 color;
                void main() {
                    uv = UV;
                    color = Color;
                    gl_Position = vec4(Position.x / GuiSize.x * 2.0 - 1.0, 1.0 - Position.y / GuiSize.y * 2.0, 0.0, 1.0);
                }
                """;
        var fragment = """
                #version 330 core
                in vec2 uv;
                in vec4 color;
                uniform sampler2D Sampler0;
                out vec4 fragColor;
                void main() {
                    fragColor = texture(Sampler0, uv) * color;
                }
                """;
        program = glCreateProgram();
        glAttachShader(program, compile(GL_VERTEX_SHADER, vertex));
        glAttachShader(program, compile(GL_FRAGMENT_SHADER, fragment));
        glLinkProgram(program);
        projection = glGetUniformLocation(program, "GuiSize");
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        ebo = glGenBuffers();
    }

    void render(List<DrawBatches.Batch> batches, int framebufferWidth, int framebufferHeight, float guiScale) {
        glEnable(GL_BLEND);
        glBlendFuncSeparate(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA, GL_ONE, GL_ONE_MINUS_SRC_ALPHA);
        glDisable(GL_CULL_FACE);
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_SCISSOR_TEST);
        glUseProgram(program);
        glUniform2f(projection, framebufferWidth / guiScale, framebufferHeight / guiScale);
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glEnableVertexAttribArray(0);
        glEnableVertexAttribArray(1);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 20, 0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 20, 8);
        glVertexAttribPointer(2, 4, GL_UNSIGNED_BYTE, true, 20, 16);

        for (var batch : batches) {
            int triangles = batch.vertexCount() / 3;
            // Quads (a, b, c, c) as Minecraft's QUADS mode would index them: (0,1,2) and (2,3,0)
            var vertices = BufferUtils.createByteBuffer(triangles * 4 * 20);
            var indices = BufferUtils.createIntBuffer(triangles * 6);
            for (int t = 0; t < triangles; t++) {
                int[] order = {t * 3, t * 3 + 1, t * 3 + 2, t * 3 + 2};
                for (int index : order) {
                    vertices.putFloat(batch.vertices()[index * 4]);
                    vertices.putFloat(batch.vertices()[index * 4 + 1]);
                    vertices.putFloat(batch.vertices()[index * 4 + 2]);
                    vertices.putFloat(batch.vertices()[index * 4 + 3]);
                    int argb = batch.colors()[index];
                    vertices.put((byte) (argb >> 16)).put((byte) (argb >> 8)).put((byte) argb).put((byte) (argb >>> 24));
                }
                int q = t * 4;
                indices.put(q).put(q + 1).put(q + 2).put(q + 2).put(q + 3).put(q);
            }
            vertices.flip();
            indices.flip();
            glBufferData(GL_ARRAY_BUFFER, vertices, GL_STREAM_DRAW);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STREAM_DRAW);
            glBindTexture(GL_TEXTURE_2D, (int) batch.textureId());
            int x = Math.round(batch.clipLeft() * guiScale);
            int y = Math.round(batch.clipTop() * guiScale);
            int w = Math.round((batch.clipRight() - batch.clipLeft()) * guiScale);
            int h = Math.round((batch.clipBottom() - batch.clipTop()) * guiScale);
            glScissor(x, framebufferHeight - y - h, w, h);
            glDrawElements(GL_TRIANGLES, triangles * 6, GL_UNSIGNED_INT, 0);
        }
        glDisable(GL_SCISSOR_TEST);
    }

    private static int compile(int type, String source) {
        int shader = glCreateShader(type);
        glShaderSource(shader, source);
        glCompileShader(shader);
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE)
            throw new IllegalStateException(glGetShaderInfoLog(shader));
        return shader;
    }
}
