package com.epam.aidial.cfg.dto.databind;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

import java.util.HashMap;
import java.util.Map;

public class JsonMapDeserializer extends ValueDeserializer<Map<String, String>> {

    @Override
    public Map<String, String> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws JacksonException {
        Map<String, String> map = new HashMap<>();

        if (jsonParser.currentToken() != JsonToken.START_OBJECT) {
            return map;
        }

        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jsonParser.currentName();
            jsonParser.nextToken();
            JsonNode treeNode = deserializationContext.readTree(jsonParser);
            map.put(fieldName, treeNode.toString());
        }

        return map;
    }
}
