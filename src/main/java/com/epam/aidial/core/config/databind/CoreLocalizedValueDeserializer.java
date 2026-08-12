package com.epam.aidial.core.config.databind;

import com.epam.aidial.core.config.CoreLocalizedValue;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

public class CoreLocalizedValueDeserializer extends JsonDeserializer<CoreLocalizedValue> {

    @Override
    public CoreLocalizedValue deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        if (p.getCurrentToken() == JsonToken.VALUE_STRING) {
            return CoreLocalizedValue.of(p.getValueAsString());
        }

        if (p.getCurrentToken() == JsonToken.START_OBJECT) {
            Map<String, String> localeMap = new LinkedHashMap<>();
            while (p.nextToken() != JsonToken.END_OBJECT) {
                String locale = p.getCurrentName();
                p.nextToken();
                localeMap.put(locale, p.getValueAsString());
            }
            return CoreLocalizedValue.of(localeMap);
        }

        if (p.getCurrentToken() == JsonToken.VALUE_NULL) {
            return null;
        }

        return ctx.reportInputMismatch(CoreLocalizedValue.class,
                "Expected a string or a locale-to-value object, got %s", p.getCurrentToken());
    }
}
