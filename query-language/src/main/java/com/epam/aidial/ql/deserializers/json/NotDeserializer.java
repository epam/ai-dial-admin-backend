package com.epam.aidial.ql.deserializers.json;

import com.epam.aidial.ql.dto.FilterDto;
import com.epam.aidial.ql.dto.filters.NotDto;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class NotDeserializer extends ValueDeserializer<NotDto> {
    @Override
    public NotDto deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        return new NotDto(ctxt.readValue(p, FilterDto.class));
    }
}
