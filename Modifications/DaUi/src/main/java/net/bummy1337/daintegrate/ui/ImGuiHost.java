package net.bummy1337.daintegrate.ui;

import imgui.ImFont;
import imgui.ImFontConfig;
import imgui.ImFontGlyphRangesBuilder;
import imgui.ImGui;
import imgui.ImGuiIO;
import imgui.callback.ImStrConsumer;
import imgui.callback.ImStrSupplier;
import imgui.flag.ImGuiBackendFlags;
import imgui.flag.ImGuiConfigFlags;
import imgui.type.ImInt;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Owns the ImGui context, fonts and theme. The platform layer (Minecraft or the preview)
 * feeds input into {@link #io()} and draws {@link ImGui#getDrawData()} after {@link #endFrame()}.
 */
public class ImGuiHost {
    /** Receives the font atlas whenever it is (re)built and returns the texture id to draw it with. */
    public interface FontTextureUploader {
        long upload(ByteBuffer rgba, int width, int height);

        void release(long textureId);
    }

    public static final String FontResource = "/assets/daintegrate/fonts/Inter-Regular.otf";
    public static final String FontBoldResource = "/assets/daintegrate/fonts/Inter-SemiBold.otf";
    public static final String FontDisplayResource = "/assets/daintegrate/fonts/Inter-ExtraBold.otf";

    public static final float BaseFontSize = 15f;
    public static final float TitleFontSize = 32f;
    public static final float HeadingFontSize = 17f;

    private final FontTextureUploader uploader;
    private final byte[] regularFont;
    private final byte[] boldFont;
    private final byte[] displayFont;

    private static final int[] LogoSizes = {48, 72, 96, 144};
    private final long[] logoTextures = new long[LogoSizes.length];

    private ImFont regular;
    private ImFont bold;
    private ImFont heading;
    private ImFont title;
    private float fontScale = -1;
    private long fontTexture;

    public ImGuiHost(FontTextureUploader uploader) {
        this.uploader = uploader;
        this.regularFont = readResource(FontResource);
        this.boldFont = readResource(FontBoldResource);
        this.displayFont = readResource(FontDisplayResource);

        ImGui.createContext();
        var io = ImGui.getIO();
        io.setIniFilename(null);
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);
        io.addBackendFlags(ImGuiBackendFlags.RendererHasVtxOffset);
        io.setConfigWindowsMoveFromTitleBarOnly(true);
        // Ctrl shortcuts on every OS, like the rest of Minecraft's text fields
        io.setConfigMacOSXBehaviors(false);
    }

    /** Lets ImGui text fields copy and paste through the platform clipboard. */
    public void setClipboard(Supplier<String> get, Consumer<String> set) {
        io().setGetClipboardTextFn(new ImStrSupplier() {
            @Override
            public String get() {
                var text = get.get();
                return text == null ? "" : text;
            }
        });
        io().setSetClipboardTextFn(new ImStrConsumer() {
            @Override
            public void accept(String text) {
                set.accept(text);
            }
        });
    }

    public ImGuiIO io() {
        return ImGui.getIO();
    }

    /**
     * Rebuilds the atlas and theme when the effective scale changes. Call before {@link ImGui#newFrame()}.
     */
    public void prepare(float scale) {
        scale = Math.round(scale * 20f) / 20f;
        if (scale == fontScale)
            return;
        fontScale = scale;

        var atlas = io().getFonts();
        if (fontTexture != 0) {
            uploader.release(fontTexture);
            fontTexture = 0;
        }
        atlas.clear();

        var ranges = glyphRanges();
        regular = addFont(regularFont, BaseFontSize * scale, ranges);
        bold = addFont(boldFont, BaseFontSize * scale, ranges);
        heading = addFont(boldFont, HeadingFontSize * scale, ranges);
        title = addFont(displayFont, TitleFontSize * scale, ranges);
        atlas.build();
        io().setFontDefault(regular);

        var width = new ImInt();
        var height = new ImInt();
        var pixels = atlas.getTexDataAsRGBA32(width, height);
        fontTexture = uploader.upload(pixels, width.get(), height.get());
        atlas.setTexID(fontTexture);

        Theme.apply(scale);
    }

    public void beginFrame(float displayWidth, float displayHeight, float deltaSeconds) {
        var io = io();
        io.setDisplaySize(displayWidth, displayHeight);
        io.setDisplayFramebufferScale(1f, 1f);
        io.setDeltaTime(deltaSeconds > 0 ? deltaSeconds : 1f / 60f);
        ImGui.newFrame();
        ImGui.pushFont(regular, regular.getLegacySize());
    }

    public void endFrame() {
        ImGui.popFont();
        ImGui.render();
    }

    public float scale() {
        return fontScale;
    }

    public ImFont regular() {
        return regular;
    }

    public ImFont bold() {
        return bold;
    }

    public ImFont heading() {
        return heading;
    }

    public ImFont title() {
        return title;
    }

    /** The logo texture closest to (and not smaller than) the given pixel size. */
    public long logo(float pixelSize) {
        int index = LogoSizes.length - 1;
        for (int i = 0; i < LogoSizes.length; i++) {
            if (LogoSizes[i] >= pixelSize) {
                index = i;
                break;
            }
        }
        if (logoTextures[index] == 0)
            logoTextures[index] = loadImage("/assets/daintegrate/textures/logo_" + LogoSizes[index] + ".png");
        return logoTextures[index];
    }

    private long loadImage(String path) {
        try (InputStream stream = ImGuiHost.class.getResourceAsStream(path)) {
            if (stream == null)
                throw new IllegalStateException("Missing resource " + path);
            BufferedImage image = ImageIO.read(stream);
            int width = image.getWidth();
            int height = image.getHeight();
            var pixels = ByteBuffer.allocateDirect(width * height * 4);
            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int argb = image.getRGB(x, y);
                    pixels.put((byte) (argb >> 16)).put((byte) (argb >> 8)).put((byte) argb).put((byte) (argb >>> 24));
                }
            }
            pixels.flip();
            return uploader.upload(pixels, width, height);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + path, e);
        }
    }

    public void dispose() {
        if (fontTexture != 0)
            uploader.release(fontTexture);
        fontTexture = 0;
        for (int i = 0; i < logoTextures.length; i++) {
            if (logoTextures[i] != 0)
                uploader.release(logoTextures[i]);
            logoTextures[i] = 0;
        }
        ImGui.destroyContext();
    }

    private ImFont addFont(byte[] data, float size, short[] ranges) {
        var config = new ImFontConfig();
        config.setFontDataOwnedByAtlas(false);
        config.setOversampleH(2);
        config.setOversampleV(2);
        config.setPixelSnapH(true);
        var font = io().getFonts().addFontFromMemoryTTF(data, size, config, ranges);
        config.destroy();
        return font;
    }

    private short[] glyphRanges() {
        var builder = new ImFontGlyphRangesBuilder();
        builder.addRanges(io().getFonts().getGlyphRangesDefault());
        builder.addRanges(io().getFonts().getGlyphRangesCyrillic());
        // Punctuation (… — •), the rouble sign, arrows, infinity and check marks
        builder.addRanges(new short[]{0x2010, 0x203A, 0x20AC, 0x20BD, 0x2116, 0x2116, 0x2190, 0x2193, 0x221E, 0x221E, 0x2212, 0x2212, 0x2713, 0x2715, 0});
        return builder.buildRanges();
    }

    private static byte[] readResource(String path) {
        try (InputStream stream = ImGuiHost.class.getResourceAsStream(path)) {
            if (stream == null)
                throw new IllegalStateException("Missing resource " + path);
            return stream.readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read " + path, e);
        }
    }
}
