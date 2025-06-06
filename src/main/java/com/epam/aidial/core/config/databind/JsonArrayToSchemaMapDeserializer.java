package com.epam.aidial.core.config.databind;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.node.NullNode;

public class JsonArrayToSchemaMapDeserializer extends JsonDeserializer<Map<String, String>> {

    @Override
    public Map<String, String> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        TreeNode tree = jsonParser.readValueAsTree();
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
            String schemaId = valueNode.get("$id").asText();
            result.put(schemaId, valueNode.toString());
        }
        return result;
    }
}