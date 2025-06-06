package com.epam.aidial.core.config.databind;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class StringToJsonSerializer extends JsonSerializer<String> {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Override
    public void serialize(String value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (value == null) {
            log.trace("Serializing value as null");
            gen.writeNull();
            return;
        }

        try {
            Object jsonObject = OBJECT_MAPPER.readValue(value, Object.class);
            log.trace("Serializing value as JSON object. Value: '{}'", value);
            gen.writeObject(jsonObject);
        } catch (IOException e) {
            log.trace("Failed to serializing value as JSON object. Writing it as string. Value: {}", value);
            gen.writeString(value);
        }
    }
}