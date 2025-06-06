package com.epam.aidial.cfg.dto.databind;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.util.Map;

public class JsonMapSerializer extends JsonSerializer<Map<String, String>> {

    @Override
    public void serialize(Map<String, String> map, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        jsonGenerator.writeStartObject();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            jsonGenerator.writeFieldName(entry.getKey());
            jsonGenerator.writeRawValue(entry.getValue());
        }

        jsonGenerator.writeEndObject();
    }
}
