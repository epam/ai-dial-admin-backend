package com.epam.aidial.core.config.databind;

import java.util.HashMap;
import java.util.Map;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.TreeNode;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;
import tools.jackson.databind.exc.InvalidFormatException;
import tools.jackson.databind.node.NullNode;

public class JsonArrayToSchemaMapDeserializer extends ValueDeserializer<Map<String, String>> {

    @Override
    public Map<String, String> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws JacksonException {
        TreeNode tree = deserializationContext.readTree(jsonParser);
        if (!tree.isArray()) {
            throw InvalidFormatException.from(jsonParser, "Expected a JSON array of schemas", tree.toString(), Map.class);
        }
        Map<String, String> result = new HashMap<>();
        for (int i = 0; i < tree.size(); i++) {
            TreeNode value = tree.get(i);
            if (value instanceof NullNode) {
                throw InvalidFormatException.from(jsonParser, Map.class, "Null value is not expected in schema array");
            }
            if (!value.isObject()) {
                throw InvalidFormatException.from(jsonParser, Map.class, "Non object value is not expected in schema array");
            }
            JsonNode valueNode = (JsonNode) value;
            if (!valueNode.has("$id")) {
                throw new InvalidFormatException(jsonParser, "JSON Schema for the custom app should have $id property",
                        valueNode.toPrettyString(), Map.class);
            }
            String schemaId = valueNode.get("$id").asString();
            result.put(schemaId, valueNode.toString());
        }
        return result;
    }
}