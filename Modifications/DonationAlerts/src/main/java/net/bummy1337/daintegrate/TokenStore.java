package net.bummy1337.daintegrate;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.regex.Pattern;

/**
 * Keeps the Donation Alerts secret token in the user's home directory and
 * understands both a bare token and any Donation Alerts link that carries one
 * (for example the alert widget link for OBS).
 */
public class TokenStore {
    private static final Pattern TokenParameter = Pattern.compile("[?&#]token=([^&#\\s]+)");
    private static final Pattern BareToken = Pattern.compile("[A-Za-z0-9_\\-]{8,}");

    private final File file;

    public TokenStore() {
        this(new File(System.getProperty("user.home"), Constants.TokenFileName));
    }

    public TokenStore(File file) {
        this.file = file;
    }

    /**
     * Extracts a token from user input. Returns null when the input does not look like one.
     */
    public static String parse(String input) {
        if (input == null)
            return null;
        var text = input.trim();
        if (text.isEmpty())
            return null;
        var matcher = TokenParameter.matcher(text);
        if (matcher.find())
            text = URLDecoder.decode(matcher.group(1), StandardCharsets.UTF_8);
        return BareToken.matcher(text).matches() ? text : null;
    }

    public static String mask(String token) {
        if (token == null || token.isEmpty())
            return "";
        // Only the tail is shown: the menu may be visible on stream
        if (token.length() <= 8)
            return "•".repeat(8);
        return "•".repeat(8) + token.substring(token.length() - 4);
    }

    public String load() {
        if (!file.exists())
            return null;
        try {
            for (var line : Files.readAllLines(file.toPath(), StandardCharsets.UTF_8)) {
                var token = parse(line);
                if (token != null)
                    return token;
            }
        } catch (IOException ignored) {
        }
        return null;
    }

    public boolean exists() {
        return load() != null;
    }

    public void save(String token) throws IOException {
        Files.writeString(file.toPath(), token + System.lineSeparator(), StandardCharsets.UTF_8);
    }

    public void delete() {
        if (file.exists())
            file.delete();
    }

    public File getFile() {
        return file;
    }
}
