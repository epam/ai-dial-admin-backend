package com.epam.aidial.cfg.service.config.transfer;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class VersionAwareSchemaChecker {

    private final VersionedSchemaLoader schemaLoader;

    /**
     * Pre-validates that a schema exists for the given version.
     * Throws InvalidVersionException if no compatible schema is available.
     * Call this upfront so errors surface as exit-2 rather than per-file errors.
     */
    public void preloadSchema(String version) {
        schemaLoader.loadSchema(version);
    }

    /**
     * Returns dot-notation paths of fields in {@code rawNode} that are absent
     * from the schema for the given Core version.
     */
    public List<String> check(JsonNode rawNode, String version) {
        JsonNode schema = schemaLoader.loadSchema(version);
        List<String> violations = new ArrayList<>();
        collectViolations(rawNode, schema, "", schema, version, violations);
        return violations;
    }

    private void collectViolations(JsonNode node, JsonNode schema, String path,
                                   JsonNode root, String version, List<String> violations) {
        if (node == null || !node.isObject()) {
            return;
        }

        JsonNode resolvedSchema = resolveRef(schema, root);

        // patternProperties = map-like object: keys are arbitrary, recurse into values
        if (resolvedSchema.has("patternProperties")) {
            JsonNode patternProps = resolvedSchema.get("patternProperties");
            JsonNode valueSchema = patternProps.elements().hasNext()
                    ? patternProps.elements().next()
                    : null;
            if (valueSchema != null) {
                node.fields().forEachRemaining(e -> {
                    String childPath = path.isEmpty() ? e.getKey() : path + "." + e.getKey();
                    collectViolations(e.getValue(), valueSchema, childPath, root, version, violations);
                });
            }
            return;
        }

        Map<String, JsonNode> allowedFields = resolveProperties(resolvedSchema, root);

        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> entry = fields.next();
            String fieldName = entry.getKey();
            JsonNode fieldValue = entry.getValue();
            String fieldPath = path.isEmpty() ? fieldName : path + "." + fieldName;

            JsonNode fieldSchema = allowedFields.get(fieldName);
            if (fieldSchema == null) {
                violations.add("Field '" + fieldPath + "' is not supported by Core version " + version);
            } else {
                collectViolations(fieldValue, fieldSchema, fieldPath, root, version, violations);
            }
        }
    }

    private Map<String, JsonNode> resolveProperties(JsonNode schema, JsonNode root) {
        Map<String, JsonNode> props = new HashMap<>();

        if (schema.has("properties")) {
            schema.get("properties").fields()
                    .forEachRemaining(e -> props.put(e.getKey(), e.getValue()));
        }
        if (schema.has("allOf")) {
            for (JsonNode sub : schema.get("allOf")) {
                props.putAll(resolveProperties(resolveRef(sub, root), root));
            }
        }
        if (schema.has("$ref")) {
            props.putAll(resolveProperties(resolveRef(schema, root), root));
        }
        return props;
    }

    private JsonNode resolveRef(JsonNode schema, JsonNode root) {
        if (!schema.has("$ref")) {
            return schema;
        }
        String ref = schema.get("$ref").asText();
        if (ref.startsWith("#/definitions/")) {
            String definitionName = ref.substring("#/definitions/".length());
            JsonNode definitions = root.get("definitions");
            if (definitions != null && definitions.has(definitionName)) {
                return definitions.get(definitionName);
            }
        }
        return schema;
    }
}
