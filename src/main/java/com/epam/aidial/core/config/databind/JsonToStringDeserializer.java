package com.epam.aidial.core.config.databind;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class JsonToStringDeserializer extends ValueDeserializer<String> {

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctx) throws JacksonException {
        if (p.currentToken() == JsonToken.VALUE_STRING) {
            return p.getValueAsString();
        }

        return ctx.readTree(p).toString();
    }
}
