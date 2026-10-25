package net.bummy1337.daintegrate.configurations.sources;

import net.bummy1337.daintegrate.Constants;
import net.bummy1337.daintegrate.DonationAlertsIntegrate;
import net.bummy1337.daintegrate.IConfigurationSource;
import net.bummy1337.daintegrate.ITransformer;
import net.bummy1337.daintegrate.configurations.*;
import net.bummy1337.daintegrate.handlers.CommandHandlerProperties;
import net.bummy1337.daintegrate.handlers.MessageHandlerProperties;
import net.bummy1337.daintegrate.listeners.IListener;
import net.bummy1337.daintegrate.sensitives.DonateSensitiveProperties;
import net.bummy1337.daintegrate.sensitives.SubscribeSensitiveProperties;
import net.bummy1337.daintegrate.sensitives.TwitchBitsSensitiveProperties;
import net.bummy1337.daintegrate.sensitives.TwitchPointsSensitiveProperties;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Representer;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

/**
 * Settings file that the menu writes and the triggers read. The file watcher and the menu run on
 * different threads, so every read and write of the file goes through one lock: otherwise the watcher
 * could read a half-written file and leave the triggers that fire different from those in the menu.
 */
public class FileConfigurationSource implements IConfigurationSource {
    private final String path;
    private final String watchedFileName;
    private final WatchService watcher;
    private final ArrayList<IListener<SettingsDto>> listeners;
    private final ITransformer<String, SettingsDto> transformer;
    private final IListener<String> log;
    private final MessageDigest md;
    private volatile SettingsDto current;

    {
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private Thread thread;
    private boolean pooling;
    private byte[] currentHash;

    public FileConfigurationSource(
            String path,
            String file,
            ArrayList<IListener<SettingsDto>> listeners,
            ITransformer<String, SettingsDto> transformer,
            IListener<String> log) throws IOException {
        this.transformer = transformer;
        this.log = log;
        var cp = Paths.get(path);
        createIfNotExists(path, file);
        this.path = cp.toString();
        watchedFileName = file;
        watcher = FileSystems.getDefault().newWatchService();
        // Saving goes through a temporary file and a rename, which shows up as a new file
        cp.register(watcher, ENTRY_CREATE, ENTRY_MODIFY);
        this.listeners = listeners;
    }

    private void createIfNotExists(String path, String file) {
        var resultMkdirs = new File(path).mkdirs();
        var fileHandle = new File(path, file);
        if (!fileHandle.exists())
        {
            try {
                var result = fileHandle.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            try {
                var alwaysProperties = new PropertiesDto();
                var handlerMessageProperties = new PropertiesDto();
                alwaysProperties.type = Constants.ModId + "/sensitive/always";
                var always = new SensitivePropertiesDto();
                always.properties = alwaysProperties;
                var trigger = new TriggerDto();

                var messageProps = new MessageHandlerProperties();
                messageProps.message = "Hello! This is an example message for all events from Donation Alerts";
                handlerMessageProperties.type = Constants.ModId + "/handler/message";
                handlerMessageProperties.value = messageProps;
                var message = new HandlerPropertiesDto();
                message.properties = handlerMessageProperties;
                trigger.sensitives = new SensitivePropertiesDto[1];
                trigger.sensitives[0] = always;
                trigger.isActive = true;
                trigger.name = "default";
                trigger.description = "example trigger, check " + Constants.GuideToConfiguration + " to learn how to set up this";
                trigger.handlers = new HandlerPropertiesDto[1];
                trigger.handlers[0] = message;

                var defaultSettings = new SettingsDto();
                defaultSettings.triggers = new TriggerDto[1];
                defaultSettings.triggers[0] = trigger;

                var options = new DumperOptions();
                options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                var representer = new Representer(options);
                representer.addClassTag(SettingsDto.class, Tag.MAP);
                representer.addClassTag(MessageHandlerProperties.class, Tag.MAP);
                var yaml = new Yaml(representer, options);
                PrintWriter writer = new PrintWriter(fileHandle, StandardCharsets.UTF_8);
                writer.print(yaml.dump(defaultSettings));
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void onValue(SettingsDto value) {
        for (var listener : listeners)
            listener.onValue(value);
    }

    @Override
    public void startListening() {
        if (pooling)
            return;
        pooling = true;
        tryUpdate();
        thread = new Thread(() -> {
            while (pooling) {
                WatchKey key = null;
                try {
                    key = watcher.take();
                    for (WatchEvent<?> e : key.pollEvents()) {
                        var event = (WatchEvent<Path>)e;
                        WatchEvent.Kind<?> kind = event.kind();
                        if (kind == OVERFLOW)
                            continue;
                        Path file = event.context().getFileName();
                        if (!file.toString().equals(watchedFileName))
                            continue;
                        tryUpdate();
                    }
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
                finally {
                    if (key != null && !key.reset())
                        break;
                }
            }
        });
        thread.start();
    }

    /** Re-reads the settings file even if it looks unchanged. */
    public synchronized void reload() {
        currentHash = null;
        tryUpdate();
    }

    public File getFile() {
        return new File(path, watchedFileName);
    }

    private synchronized void tryUpdate() {
        try {
            String json = null;
            json = Files.readString(Paths.get(path, watchedFileName));
            if (json == null)
                return;
            var hash = hash(json);
            if (Arrays.equals(hash, currentHash))
                return;
            var value = transformer.transform(json);
            if (value == null)
                return;
            // Remember the file only once it is read, so a file caught mid-write by another program is read again
            currentHash = hash;
            current = value;
            onValue(value);
        } catch (Exception e) {
            log.onValue("error while update the settings from file: " + e);
        }
    }

    private byte[] hash(String text) {
        return md.digest(text.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void stopListening() {
        try {
            watcher.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        pooling = false;
    }

    public void addListener(IListener<SettingsDto> listener) {
        listeners.add(listener);
    }

    public SettingsDto getCurrent() {
        return current;
    }

    public synchronized void save(SettingsDto settings) throws IOException {
        var options = new DumperOptions();
        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
        var representer = new Representer(options);
        representer.addClassTag(SettingsDto.class, Tag.MAP);
        representer.addClassTag(TriggerDto.class, Tag.MAP);
        representer.addClassTag(HandlerPropertiesDto.class, Tag.MAP);
        representer.addClassTag(SensitivePropertiesDto.class, Tag.MAP);
        representer.addClassTag(PropertiesDto.class, Tag.MAP);
        representer.addClassTag(MessageHandlerProperties.class, Tag.MAP);
        representer.addClassTag(CommandHandlerProperties.class, Tag.MAP);
        representer.addClassTag(DonateSensitiveProperties.class, Tag.MAP);
        representer.addClassTag(SubscribeSensitiveProperties.class, Tag.MAP);
        representer.addClassTag(TwitchPointsSensitiveProperties.class, Tag.MAP);
        representer.addClassTag(TwitchBitsSensitiveProperties.class, Tag.MAP);
        // Settings of every action, including those other mods add
        for (var action : DonationAlertsIntegrate.getActions())
            representer.addClassTag(action.getProperties().getType(), Tag.MAP);
        var yaml = new Yaml(representer, options);
        var output = yaml.dump(settings);
        // Known before the watcher sees the change, so it does not read the file back
        currentHash = hash(output);
        write(output);
        current = settings;
        onValue(settings);
    }

    /** Replaces the file in one step, so nobody ever reads it half-written. */
    private void write(String content) throws IOException {
        var target = Paths.get(path, watchedFileName);
        var temporary = Paths.get(path, watchedFileName + ".tmp");
        Files.writeString(temporary, content, StandardCharsets.UTF_8);
        try {
            Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            // The file is held by another program (an antivirus, an editor): write it in place
            Files.deleteIfExists(temporary);
            Files.writeString(target, content, StandardCharsets.UTF_8);
        }
    }
}
