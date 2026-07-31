package com.epam.aidial.cfg.dto.databind;

import com.epam.aidial.cfg.domain.value.LocalizedValue;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;

/**
 * Custom Jackson serializer for {@link LocalizedValue}.
 * Outputs either a plain string or a locale map object depending on the internal representation.
 *
 * <p>Output formats:</p>
 * <pre>
 * // Plain string or single-locale map (collapsed)
 * "GPT-4"
 *
 * // Multi-locale map
 * {"en": "GPT-4", "fr": "GPT-4", "de": "GPT-4"}
 * </pre>
 *
 * @since 0.47.0
 */
public class LocalizedValueSerializer extends JsonSerializer<LocalizedValue> {

    @Override
    public void serialize(LocalizedValue value, JsonGenerator gen, SerializerProvider serializers) throws IOException {
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
