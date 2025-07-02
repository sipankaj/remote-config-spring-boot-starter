package io.github.sipankaj.remoteconfig.loader.interfaces;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

public interface IRemoteConfigLoader {
    public void load() throws Exception;
    public static void applyToEnvironment(InputStream input, String fileName, ConfigurableEnvironment env) throws Exception {

        if (fileName.endsWith(".yml") || fileName.endsWith(".yaml")) {
            Yaml yaml = new Yaml();
            Map<String, Object> raw = yaml.load(input);
            if (raw != null) {
                Map<String, Object> flat = flatten(raw, null);
                env.getPropertySources().addFirst(new MapPropertySource(fileName, flat));
            }
        } else if (fileName.endsWith(".properties")) {
            Properties props = new Properties();
            props.load(input);
            Map<String, Object> map = new HashMap<>();
            for (String key : props.stringPropertyNames()) {
                map.put(key, props.getProperty(key));
            }
            env.getPropertySources().addFirst(new MapPropertySource(fileName, map));
        } else {
            throw new UnsupportedOperationException("Unsupported file type: " + fileName);
        }
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> flatten(Map<String, Object> source, String parentKey) {
        Map<String, Object> result = new LinkedHashMap<>();

        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = (parentKey == null) ? entry.getKey() : parentKey + "." + entry.getKey();
            Object value = entry.getValue();

            if (value instanceof Map<?, ?> nestedMap) {
                result.putAll(flatten((Map<String, Object>) nestedMap, key));
            } else {
                result.put(key, value);
            }
        }

        return result;
    }
}
