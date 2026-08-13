package com.epam.aidial.cfg.dto.databind;

import com.epam.aidial.cfg.dto.LocalizedValueDto;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

public class LocalizedValueSerializer extends JsonSerializer<LocalizedValueDto> {

    @Override
    public void serialize(LocalizedValueDto value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
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
