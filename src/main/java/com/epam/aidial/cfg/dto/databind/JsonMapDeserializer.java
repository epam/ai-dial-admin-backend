package com.epam.aidial.cfg.dto.databind;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class JsonMapDeserializer extends JsonDeserializer<Map<String, String>> {

    @Override
    public Map<String, String> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        Map<String, String> map = new HashMap<>();

        if (jsonParser.getCurrentToken() != JsonToken.START_OBJECT) {
            return map;
        }

        while (jsonParser.nextToken() != JsonToken.END_OBJECT) {
            String fieldName = jsonParser.currentName();
            jsonParser.nextToken();
            JsonNode treeNode = jsonParser.readValueAsTree();
            map.put(fieldName, treeNode.toString());
        }

        return map;
    }
}
