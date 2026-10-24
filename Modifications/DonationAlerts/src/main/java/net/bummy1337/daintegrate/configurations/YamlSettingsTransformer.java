package net.bummy1337.daintegrate.configurations;

import com.google.gson.Gson;
import net.bummy1337.daintegrate.Constants;
import net.bummy1337.daintegrate.DonationAlertsIntegrate;
import net.bummy1337.daintegrate.ITransformer;
import org.yaml.snakeyaml.Yaml;

import java.util.LinkedHashMap;

public class YamlSettingsTransformer implements ITransformer<String, SettingsDto> {
    private static final Gson gson = new Gson();

    @Override
    public SettingsDto transform(String input) {
        input = input.replaceAll("!!net\\.(folleach|bummy1337)\\.daintegrate\\.\\S+", "");
        var yaml = new Yaml();
        var result = yaml.loadAs(input, SettingsDto.class);
        if (result.triggers == null)
            return result;
        for (var trigger : result.triggers) {
            if (trigger == null)
                continue;
            if (trigger.handlers == null)
                trigger.handlers = new HandlerPropertiesDto[0];
            if (trigger.sensitives == null)
                trigger.sensitives = new SensitivePropertiesDto[0];
            for (var handler : trigger.handlers) {
                if (handler == null)
                    continue;
                if (handler.properties != null)
                    replaceHashMap(handler.properties);
            }
            for (var sensitive : trigger.sensitives) {
                if (sensitive == null)
                    continue;
                if (sensitive.properties != null)
                    replaceHashMap(sensitive.properties);
            }
        }
        return result;
    }

    // (аё‡ в—‰ _ в—‰)аё‡ Even the UI in the first versions of the mod will not be worse than this
    @SuppressWarnings("unchecked")
    private void replaceHashMap(PropertiesDto properties) {
        var typeName = properties.type;

        if (!DonationAlertsIntegrate.knownEntity(typeName)) {
            var mapped = mapLegacyType(typeName);
            if (mapped != null) {
                typeName = mapped;
                properties.type = typeName;
            }
        }

        // A type from a mod that is not installed: keep its settings as they are, so saving
        // from the menu does not lose them, and the trigger skips it at run time
        if (!DonationAlertsIntegrate.knownEntity(typeName))
            return;

        if (properties.value instanceof String) {
            var legacyValue = (String) properties.value;
            var map = new LinkedHashMap<String, Object>();
            if (typeName.endsWith("/handler/message"))
                map.put("message", legacyValue);
            else if (typeName.endsWith("/handler/command"))
                map.put("command", legacyValue);
            properties.value = map;
        }

        if (properties.value == null)
            properties.value = new LinkedHashMap<String, Object>();
        var map = (LinkedHashMap<String, Object>)properties.value;
        var type = DonationAlertsIntegrate.getProperties(typeName).getType();
        var json = gson.toJson(map);
        properties.value = gson.fromJson(json, type);
    }

    private static String mapLegacyType(String typeName) {
        switch (typeName) {
            case "donate":        return Constants.ModId + "/sensitive/donate";
            case "always":        return Constants.ModId + "/sensitive/always";
            case "subscribe":     return Constants.ModId + "/sensitive/subscribe";
            case "twitch/bits":
            case "twitch:bits":   return Constants.ModId + "/sensitive/twitch/bits";
            case "twitch/points":
            case "twitch:points": return Constants.ModId + "/sensitive/twitch/points";
            case "message":       return Constants.ModId + "/handler/message";
            case "command":       return Constants.ModId + "/handler/command";
        }
        return null;
    }
}
