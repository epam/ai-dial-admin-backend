package com.epam.aidial.cfg.service.transfer;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.exception.SchemaValidationException;
import com.epam.aidial.core.config.Config;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Filters config objects based on JSON schema definitions for specific versions.
 * Ensures only fields defined in the schema are included in the exported config.
 */
@Slf4j
@Service
@LogExecution
@RequiredArgsConstructor
public class VersionAwareFieldFilter {

    private static final String APPLICATION_TYPE_SCHEMAS_KEY = "applicationTypeSchemas";

    private final CoreConfigVersionAutoDetectService versionAutoDetectService;
    private final VersionedSchemaLoader schemaLoader;
    private final ObjectMapper objectMapper;

    /**
     * Filters a Config object to include only fields defined in the schema for the specified version.
     *
     * @param config  The config to filter by target version
     * @return Filtered config with only schema-defined fields
     */
    public Config filterForTargetVersion(Config config) {
        String version = versionAutoDetectService.getVersion();
        try {
            JsonNode schema = schemaLoader.loadSchema(version);
            String configJson = objectMapper.writeValueAsString(config);
            JsonNode configNode = objectMapper.readTree(configJson);
            JsonNode filteredNode = filterNodeBySchema(configNode, schema);
            return objectMapper.treeToValue(filteredNode, Config.class);

        } catch (Exception e) {
            log.error("Failed to filter config for version: {}", version, e);
            throw new SchemaValidationException("Failed to filter config for version: %s".formatted(version), e);
        }
    }

    /**
     * Recursively filters a JSON node according to the provided schema.
     * Only fields defined in the schema will be included in the result.
     *
     * @param node   The node to filter
     * @param schema The schema to filter against
     * @return A filtered copy of the node
     */
    private JsonNode filterNodeBySchema(JsonNode node, JsonNode schema) {
        if (!node.isObject() || !schema.isObject()) {
            return node;
        }

        ObjectNode filteredNode = objectMapper.createObjectNode();

        processSchemaInheritance(node, schema, filteredNode);
        processSchemaReferences(node, schema, filteredNode);
        processProperties(node, schema, filteredNode);

        return filteredNode;
    }
    
    /**
     * Processes schema inheritance via allOf directive.
     */
    private void processSchemaInheritance(JsonNode node, JsonNode schema, ObjectNode filteredNode) {
        if (!schema.has("allOf")) {
            return;
        }

        JsonNode allOf = schema.get("allOf");
        if (allOf.isArray()) {
            for (JsonNode parentSchema : allOf) {
                JsonNode parentFiltered = filterNodeBySchema(node, parentSchema);
                copyFields(parentFiltered, filteredNode);
            }
        }
    }
    
    /**
     * Processes schema references ($ref).
     */
    private void processSchemaReferences(JsonNode node, JsonNode schema, ObjectNode filteredNode) {
        if (!schema.has("$ref")) {
            return;
        }

        String ref = schema.get("$ref").asText();
        if (ref.startsWith("#/definitions/")) {
            String definitionName = ref.substring("#/definitions/".length());
            JsonNode rootSchema = findRootSchema(schema);
            JsonNode definitions = rootSchema.get("definitions");
            if (definitions != null && definitions.has(definitionName)) {
                JsonNode refFiltered = filterNodeBySchema(node, definitions.get(definitionName));
                copyFields(refFiltered, filteredNode);
            }
        }
    }

    /**
     * Processes direct properties defined in the schema.
     */
    private void processProperties(JsonNode node, JsonNode schema, ObjectNode filteredNode) {
        JsonNode properties = schema.get("properties");
        if (properties != null && properties.isObject()) {
            // Create a map of camelCase and snake_case field names to their schema definitions
            Map<String, JsonNode> fieldSchemaMap = new HashMap<>();

            Iterator<Map.Entry<String, JsonNode>> schemaFields = properties.fields();
            while (schemaFields.hasNext()) {
                Map.Entry<String, JsonNode> entry = schemaFields.next();
                String schemaFieldName = entry.getKey();
                JsonNode fieldSchema = entry.getValue();
                fieldSchemaMap.put(schemaFieldName, fieldSchema);

                String snakeCase = camelToSnakeCase(schemaFieldName);
                if (!snakeCase.equals(schemaFieldName)) {
                    fieldSchemaMap.put(snakeCase, fieldSchema);
                }
            }
            
            // Process node fields
            Iterator<Map.Entry<String, JsonNode>> nodeFields = node.fields();
            while (nodeFields.hasNext()) {
                Map.Entry<String, JsonNode> nodeField = nodeFields.next();
                String fieldName = nodeField.getKey();
                JsonNode fieldValue = nodeField.getValue();

                // Check if field exists in schema
                if (fieldSchemaMap.containsKey(fieldName)) {
                    JsonNode fieldSchema = fieldSchemaMap.get(fieldName);

                    // Use the original field name from the node for the filtered output
                    processField(fieldName, fieldValue, fieldSchema, schema, filteredNode);
                }
                // Fields not in schema are excluded
            }
        }
    }
    
    /**
     * Converts a camelCase string to snake_case.
     *
     * @param camel The camelCase string
     * @return The snake_case version
     */
    private String camelToSnakeCase(String camel) {
        if (camel == null || camel.isEmpty()) {
            return camel;
        }
        
        StringBuilder snake = new StringBuilder();
        char c = camel.charAt(0);
        snake.append(Character.toLowerCase(c));
        
        for (int i = 1; i < camel.length(); i++) {
            c = camel.charAt(i);
            if (Character.isUpperCase(c)) {
                snake.append('_');
                snake.append(Character.toLowerCase(c));
            } else {
                snake.append(c);
            }
        }
        
        return snake.toString();
    }
    
    /**
     * Processes a single field based on its schema.
     */
    private void processField(String fieldName, JsonNode fieldValue, JsonNode fieldSchema,
                              JsonNode parentSchema, ObjectNode filteredNode) {

        // Value of application type schema is stored as JSON string instead of object,
        // so it needs to be unwrapped from array and wrapped to map - to conform with other entities
        String appTypeSchemaId = null;
        if (APPLICATION_TYPE_SCHEMAS_KEY.equals(fieldName)) {
            JsonNode unwrappedFieldValue = unwrapAppTypeSchemaNode(fieldName, fieldValue, fieldSchema, parentSchema, filteredNode);
            if (unwrappedFieldValue == null || unwrappedFieldValue.get("$id") == null) {
                return;
            }
            ObjectNode fieldValueWrappedAsMap = objectMapper.createObjectNode();
            appTypeSchemaId = unwrappedFieldValue.get("$id").asText();
            fieldValueWrappedAsMap.set(appTypeSchemaId, unwrappedFieldValue);
            fieldValue = fieldValueWrappedAsMap;
        }

        if (!fieldValue.isObject()) {
            filteredNode.set(fieldName, fieldValue);
            return;
        }

        if (fieldSchema.has("properties")) {
            filteredNode.set(fieldName, filterNodeBySchema(fieldValue, fieldSchema));
        } else if (fieldSchema.has("patternProperties")) {
            JsonNode filteredProperties = filterPatternProperties(fieldValue, fieldSchema);
            if (APPLICATION_TYPE_SCHEMAS_KEY.equals(fieldName)) {
                // Wrapping application type schema back to array
                if (filteredNode.get(APPLICATION_TYPE_SCHEMAS_KEY) == null) {
                    ArrayNode arrayWrapper = objectMapper.createArrayNode();
                    arrayWrapper.add(filteredProperties.get(appTypeSchemaId));
                    filteredNode.set(APPLICATION_TYPE_SCHEMAS_KEY, arrayWrapper);
                } else {
                    ArrayNode appTypeSchemasArrayNode = (ArrayNode) filteredNode.get(APPLICATION_TYPE_SCHEMAS_KEY);
                    appTypeSchemasArrayNode.add(filteredProperties.get(appTypeSchemaId));
                    filteredNode.set(APPLICATION_TYPE_SCHEMAS_KEY, appTypeSchemasArrayNode);
                }
            } else {
                filteredNode.set(fieldName, filteredProperties);
            }
        } else if (fieldSchema.has("$ref")) {
            processFieldReference(fieldName, fieldValue, fieldSchema, parentSchema, filteredNode);
        } else {
            filteredNode.set(fieldName, fieldValue);
        }
    }

    /**
     * Unwraps application type schema node.
     */
    private JsonNode unwrapAppTypeSchemaNode(String fieldName, JsonNode fieldValue, JsonNode fieldSchema,
                                             JsonNode parentSchema, ObjectNode filteredNode) {
        if (fieldValue == null) {
            return null;
        }

        if (fieldValue.isObject() || !fieldValue.isArray()) {
            return fieldValue;
        }

        if (fieldValue.size() >= 1) {
            Iterator<JsonNode> nodeElements = fieldValue.elements();
            while (nodeElements.hasNext()) {
                processField(fieldName, nodeElements.next(), fieldSchema, parentSchema, filteredNode);
            }
        }

        return fieldValue;
    }
    
    /**
     * Processes a field that references another schema definition.
     */
    private void processFieldReference(String fieldName, JsonNode fieldValue, JsonNode fieldSchema, 
                                       JsonNode parentSchema, ObjectNode filteredNode) {
        String ref = fieldSchema.get("$ref").asText();
        if (!ref.startsWith("#/definitions/")) {
            filteredNode.set(fieldName, fieldValue);
            return;
        }

        String definitionName = ref.substring("#/definitions/".length());
        JsonNode rootSchema = findRootSchema(parentSchema);
        JsonNode definitions = rootSchema.get("definitions");
        if (definitions != null && definitions.has(definitionName)) {
            filteredNode.set(fieldName, filterNodeBySchema(fieldValue, definitions.get(definitionName)));
        } else {
            filteredNode.set(fieldName, fieldValue);
        }
    }
    
    /**
     * Utility method to copy fields from one node to another.
     */
    private void copyFields(JsonNode source, ObjectNode target) {
        if (!source.isObject()) {
            return;
        }

        Iterator<Map.Entry<String, JsonNode>> fields = source.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            target.set(field.getKey(), field.getValue());
        }
    }

    /**
     * Filters a node that follows a pattern property schema.
     * Used for map-like objects where keys follow a pattern and values follow a schema.
     *
     * @param node   The node to filter
     * @param schema The schema with patternProperties
     * @return A filtered copy of the node
     */
    private JsonNode filterPatternProperties(JsonNode node, JsonNode schema) {
        ObjectNode filteredNode = objectMapper.createObjectNode();
        JsonNode patternProperties = schema.get("patternProperties");

        if (patternProperties == null || !patternProperties.isObject()) {
            return filteredNode;
        }

        // Get the schema for the pattern (usually there's only one pattern)
        JsonNode itemSchema = patternProperties.elements().next();

        // Resolve reference if needed
        itemSchema = resolveSchemaReference(itemSchema, schema);

        // Process each field in the node according to the item schema
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String key = field.getKey();
            JsonNode value = field.getValue();

            // Filter the value according to the schema
            JsonNode filteredValue = filterNodeBySchema(value, itemSchema);
            filteredNode.set(key, filteredValue);
        }

        return filteredNode;
    }

    /**
     * Resolves a schema reference to its definition.
     *
     * @param schema       The schema that may contain a reference
     * @param parentSchema The parent schema for context
     * @return The resolved schema or the original if no reference exists
     */
    private JsonNode resolveSchemaReference(JsonNode schema, JsonNode parentSchema) {
        if (schema.has("$ref")) {
            String ref = schema.get("$ref").asText();
            if (ref.startsWith("#/definitions/")) {
                String definitionName = ref.substring("#/definitions/".length());
                JsonNode rootSchema = findRootSchema(parentSchema);
                JsonNode definitions = rootSchema.get("definitions");
                if (definitions != null && definitions.has(definitionName)) {
                    return definitions.get(definitionName);
                }
            }
        }
        return schema;
    }

    /**
     * Finds the root schema that contains definitions.
     * This is needed for resolving references to definitions.
     *
     * @param schema The current schema context
     * @return The root schema containing definitions
     */
    private JsonNode findRootSchema(JsonNode schema) {
        try {
            String version = versionAutoDetectService.getVersion();
            return schemaLoader.loadSchema(version);
        } catch (Exception e) {
            log.warn("Failed to load root schema, using current schema", e);
            return schema;
        }
    }
}