package com.epam.aidial.core.config.databind;

import com.epam.aidial.core.config.CoreLocalizedValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class CoreLocalizedValueSerializer extends JsonSerializer<CoreLocalizedValue> {

    @Override
    public void serialize(CoreLocalizedValue value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
        if (!value.isMap()) {
            gen.writeString(value.getPlainValue());
            return;
        }

        if (value.getLocaleMap().size() == 1) {
            gen.writeString(value.getLocaleMap().values().iterator().next());
            return;
        }

        gen.writeStartObject();
        for (var entry : value.getLocaleMap().entrySet()) {
            gen.writeStringField(entry.getKey(), entry.getValue());
        }
        gen.writeEndObject();
    }
}
