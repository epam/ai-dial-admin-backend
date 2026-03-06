package com.epam.aidial.cfg.service.config.transfer;

import com.epam.aidial.core.config.Config;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JsonConfigMerger {

    @JsonIgnoreProperties(ignoreUnknown = false)
    private interface StrictConfigMixin {}

    private final ObjectMapper objectMapper;

    public Config merge(List<String> filePaths) {
        return merge(filePaths, false);
    }

    public Config merge(List<String> filePaths, boolean failOnUnknownProperties) {
        if (filePaths.isEmpty()) {
            return new Config();
        }

        ObjectMapper mapper = failOnUnknownProperties
                ? objectMapper.copy()
                        .addMixIn(Config.class, StrictConfigMixin.class)
                        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                : objectMapper;

        JsonNode merged = null;
        for (String path : filePaths) {
            File file = new File(path);
            if (!file.exists()) {
                throw new IllegalArgumentException("Config file not found: " + path);
            }
            try {
                JsonNode node = mapper.readTree(file);
                merged = (merged == null) ? node : deepMerge(merged, node);
            } catch (JsonParseException e) {
                throw new IllegalArgumentException(
                        "Invalid JSON in file '" + path + "': " + e.getOriginalMessage(), e);
            } catch (IOException e) {
                throw new IllegalArgumentException("Cannot read file: " + path, e);
            }
        }

        try {
            return mapper.treeToValue(merged, Config.class);
        } catch (IOException e) {
            throw new IllegalArgumentException("Merged config cannot be deserialized: " + e.getMessage(), e);
        }
    }

    private JsonNode deepMerge(JsonNode base, JsonNode overlay) {
        if (!overlay.isObject() || !base.isObject()) {
            return overlay;
        }
        ObjectNode result = (ObjectNode) base.deepCopy();
        Iterator<Map.Entry<String, JsonNode>> fields = overlay.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String key = entry.getKey();
            JsonNode overlayValue = entry.getValue();
            JsonNode baseValue = result.get(key);
            if (baseValue != null && baseValue.isObject() && overlayValue.isObject()) {
                result.set(key, deepMerge(baseValue, overlayValue));
            } else {
                result.set(key, overlayValue);
            }
        }
        return result;
    }
}
