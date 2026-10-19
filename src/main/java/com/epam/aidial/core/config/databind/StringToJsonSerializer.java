package com.epam.aidial.core.config.databind;

import lombok.extern.slf4j.Slf4j;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
public class StringToJsonSerializer extends ValueSerializer<String> {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapper.builder().build();

    @Override
    public void serialize(String value, JsonGenerator gen, SerializationContext serializers) throws JacksonException {
        if (value == null) {
            log.trace("Serializing value as null");
            gen.writeNull();
            return;
        }

        try {
            Object jsonObject = OBJECT_MAPPER.readValue(value, Object.class);
            log.trace("Serializing value as JSON object. Value: '{}'", value);
            gen.writePOJO(jsonObject);
        } catch (JacksonException e) {
            log.trace("Failed to serializing value as JSON object. Writing it as string. Value: {}", value);
            gen.writeString(value);
        }
    }
}