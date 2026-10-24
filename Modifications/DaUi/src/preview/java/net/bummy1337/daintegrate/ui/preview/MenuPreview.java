package net.bummy1337.daintegrate.ui.preview;

import imgui.ImGui;
import imgui.gl3.ImGuiImplGl3;
import imgui.glfw.ImGuiImplGlfw;
import net.bummy1337.daintegrate.ui.DaMenu;
import net.bummy1337.daintegrate.ui.DrawBatches;
import net.bummy1337.daintegrate.ui.I18n;
import net.bummy1337.daintegrate.ui.ImGuiHost;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.GL_CLAMP_TO_EDGE;

/**
 * Runs the menu in a plain GLFW window with sample data.
 * <p>
 * Arguments: {@code --lang ru|en}, {@code --page Overview|Connection|Triggers|Settings|Help},
 * {@code --state Disconnected|Connected|Error}, {@code --no-token}, {@code --size 1280x800},
 * {@code --scale 1.0}, {@code --screenshot out.png} (renders a few frames, saves and exits),
 * {@code --game-path 2} (draws through {@link BatchRenderer} at that GUI scale, like the game does).
 */
public class MenuPreview {
    public static void main(String[] args) throws Exception {
        String lang = "ru";
        String page = null;
        String state = "Connected";
        String screenshot = null;
        boolean token = true;
        int width = 1280;
        int height = 800;
        float scale = 1f;
        float gamePath = 0;
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case "--lang" -> lang = args[++i];
                case "--page" -> page = args[++i];
                case "--state" -> state = args[++i];
                case "--screenshot" -> screenshot = args[++i];
                case "--no-token" -> token = false;
                case "--scale" -> scale = Float.parseFloat(args[++i]);
                case "--game-path" -> gamePath = Float.parseFloat(args[++i]);
                case "--size" -> {
                    var parts = args[++i].split("x");
                    width = Integer.parseInt(parts[0]);
                    height = Integer.parseInt(parts[1]);
                }
                default -> throw new IllegalArgumentException("Unknown argument " + args[i]);
            }
        }

        if (!GLFW.glfwInit())
            throw new IllegalStateException("GLFW init failed");
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);
        long window = GLFW.glfwCreateWindow(width, height, "DA Integrate menu preview", 0, 0);
        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1);
        GL.createCapabilities();

        var context = new PreviewContext(token, state, () -> GLFW.glfwSetWindowShouldClose(window, true));
        I18n.setLanguage(lang);

        var host = new ImGuiHost(new ImGuiHost.FontTextureUploader() {
            @Override
            public long upload(ByteBuffer rgba, int w, int h) {
                int texture = glGenTextures();
                glBindTexture(GL_TEXTURE_2D, texture);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
                glPixelStorei(GL_UNPACK_ROW_LENGTH, 0);
                glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, w, h, 0, GL_RGBA, GL_UNSIGNED_BYTE, rgba);
                return texture;
            }

            @Override
            public void release(long textureId) {
                glDeleteTextures((int) textureId);
            }
        });
        var glfw = new ImGuiImplGlfw();
        var gl3 = new ImGuiImplGl3();
        glfw.init(window, true);
        gl3.init("#version 330 core");

        var batchRenderer = gamePath > 0 ? new BatchRenderer() : null;
        var menu = new DaMenu(host, context);
        if (page != null)
            menu.open(DaMenu.Page.valueOf(page));

        int frames = 0;
        while (!GLFW.glfwWindowShouldClose(window)) {
            GLFW.glfwPollEvents();
            var settings = context.getSettings();
            host.prepare(scale * (settings.uiScale <= 0 ? 1f : settings.uiScale));

            int[] fbWidth = new int[1];
            int[] fbHeight = new int[1];
            GLFW.glfwGetFramebufferSize(window, fbWidth, fbHeight);
            glfw.newFrame();
            gl3.newFrame();
            host.beginFrame(fbWidth[0], fbHeight[0], ImGui.getIO().getDeltaTime());
            drawBackdrop(fbWidth[0], fbHeight[0]);
            menu.render(fbWidth[0], fbHeight[0]);
            host.endFrame();

            glViewport(0, 0, fbWidth[0], fbHeight[0]);
            // A blurred-world stand-in so the translucent backdrop reads like it will in game
            glClearColor(0.16f, 0.22f, 0.18f, 1f);
            glClear(GL_COLOR_BUFFER_BIT);
            if (batchRenderer != null)
                batchRenderer.render(DrawBatches.build(ImGui.getDrawData(), gamePath), fbWidth[0], fbHeight[0], gamePath);
            else
                gl3.renderDrawData(ImGui.getDrawData());

            if (screenshot != null && ++frames == 6) {
                save(fbWidth[0], fbHeight[0], new File(screenshot));
                break;
            }
            GLFW.glfwSwapBuffers(window);
        }

        gl3.shutdown();
        glfw.shutdown();
        host.dispose();
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
        System.exit(0);
    }

    private static void save(int width, int height, File file) throws Exception {
        var pixels = BufferUtils.createByteBuffer(width * height * 4);
        glReadBuffer(GL_BACK);
        glReadPixels(0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pixels);
        var image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int i = ((height - 1 - y) * width + x) * 4;
                int r = pixels.get(i) & 0xFF;
                int g = pixels.get(i + 1) & 0xFF;
                int b = pixels.get(i + 2) & 0xFF;
                image.setRGB(x, y, (r << 16) | (g << 8) | b);
            }
        }
        ImageIO.write(image, "png", file);
        System.out.println("Saved " + file.getAbsolutePath());
    }

    /** A soft, out-of-focus "world" (sky, sun, hills) standing in for the game's blurred background. */
    private static void drawBackdrop(float w, float h) {
        var draw = ImGui.getBackgroundDrawList();
        draw.addRectFilledMultiColor(0, 0, w, h * 0.6f, abgr(0x6FA8DC), abgr(0x8CBCE8), abgr(0xB9D4E8), abgr(0x9CC3E0));
        draw.addRectFilledMultiColor(0, h * 0.6f, w, h, abgr(0x4E7A3A), abgr(0x5B8A42), abgr(0x3A5E2C), abgr(0x34552A));
        blob(draw, w * 0.78f, h * 0.22f, h * 0.30f, 0xFFE9B0, 0.55f);
        blob(draw, w * 0.18f, h * 0.62f, h * 0.35f, 0x3F6B33, 0.8f);
        blob(draw, w * 0.55f, h * 0.70f, h * 0.40f, 0x6B8F45, 0.6f);
        blob(draw, w * 0.90f, h * 0.75f, h * 0.30f, 0x7A5A3A, 0.5f);
        blob(draw, w * 0.35f, h * 0.25f, h * 0.25f, 0xFFFFFF, 0.25f);
        draw.addRectFilled(0, 0, w, h, abgr(0x000000, 0.25f));
    }

    private static void blob(imgui.ImDrawList draw, float x, float y, float r, int rgb, float alpha) {
        for (int i = 12; i >= 1; i--)
            draw.addCircleFilled(x, y, r * i / 12f, abgr(rgb, alpha / 12f), 64);
    }

    private static int abgr(int rgb) {
        return abgr(rgb, 1f);
    }

    private static int abgr(int rgb, float alpha) {
        return net.bummy1337.daintegrate.ui.Theme.col(rgb, alpha);
    }
}
