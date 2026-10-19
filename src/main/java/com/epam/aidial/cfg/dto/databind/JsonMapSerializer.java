package com.epam.aidial.cfg.dto.databind;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.util.Map;

public class JsonMapSerializer extends ValueSerializer<Map<String, String>> {

    @Override
    public void serialize(Map<String, String> map, JsonGenerator jsonGenerator, SerializationContext serializerProvider) throws JacksonException {
        jsonGenerator.writeStartObject();

        for (Map.Entry<String, String> entry : map.entrySet()) {
            jsonGenerator.writeName(entry.getKey());
            jsonGenerator.writeRawValue(entry.getValue());
        }

        jsonGenerator.writeEndObject();
    }
}
