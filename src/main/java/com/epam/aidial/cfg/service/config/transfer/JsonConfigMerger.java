package com.epam.aidial.cfg.service.config.transfer;

import com.epam.aidial.core.config.Config;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.exc.StreamReadException;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.deser.DeserializationProblemHandler;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JsonConfigMerger {

    @JsonIgnoreProperties(ignoreUnknown = false)
    private interface StrictConfigMixin {}

    private record RawMergeResult(Config config, JsonNode rawNode) {}

    private final JsonMapper objectMapper;

    public Config merge(List<String> filePaths) {
        if (filePaths.isEmpty()) {
            return new Config();
        }
        return mergeNodes(filePaths, objectMapper).config();
    }

    public ConfigMergeResult mergeWithResult(List<String> filePaths, boolean failOnUnknownProperties) {
        if (filePaths.isEmpty()) {
            return new ConfigMergeResult(new Config(), List.of(), objectMapper.createObjectNode());
        }

        JsonMapper.Builder baseMapperBuilder = objectMapper.rebuild()
                .addMixIn(Config.class, StrictConfigMixin.class);

        if (failOnUnknownProperties) {
            JsonMapper baseMapper = baseMapperBuilder
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
                    .build();
            RawMergeResult raw = mergeNodes(filePaths, baseMapper);
            return new ConfigMergeResult(raw.config(), List.of(), raw.rawNode());
        } else {
            List<String> warnings = new ArrayList<>();
            JsonMapper baseMapper = baseMapperBuilder
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .addHandler(new DeserializationProblemHandler() {
                        @Override
                        public boolean handleUnknownProperty(DeserializationContext ctxt, JsonParser p,
                                ValueDeserializer<?> deserializer,
                                Object beanOrClass, String propertyName) throws JacksonException {
                            warnings.add("Unknown property: '" + propertyName + "'");
                            p.skipChildren();
                            return true;
                        }
                    })
                    .build();
            RawMergeResult raw = mergeNodes(filePaths, baseMapper);
            return new ConfigMergeResult(raw.config(), List.copyOf(warnings), raw.rawNode());
        }
    }

    private RawMergeResult mergeNodes(List<String> filePaths, JsonMapper mapper) {
        JsonNode merged = null;
        for (String path : filePaths) {
            File file = new File(path);
            if (!file.exists()) {
                throw new IllegalArgumentException("Config file not found: " + path);
            }
            try {
                JsonNode node = mapper.readTree(file);
                merged = (merged == null) ? node : deepMerge(merged, node);
            } catch (StreamReadException e) {
                throw new IllegalArgumentException(
                        "Invalid JSON in file '" + path + "': " + e.getOriginalMessage(), e);
            } catch (JacksonException e) {
                throw new IllegalArgumentException("Cannot read file: " + path, e);
            }
        }
        try {
            return new RawMergeResult(mapper.treeToValue(merged, Config.class), merged);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("Merged config cannot be deserialized: " + e.getMessage(), e);
        }
    }

    private JsonNode deepMerge(JsonNode base, JsonNode overlay) {
        if (!overlay.isObject() || !base.isObject()) {
            return overlay;
        }
        ObjectNode result = (ObjectNode) base.deepCopy();
        for (var entry : overlay.properties()) {
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
