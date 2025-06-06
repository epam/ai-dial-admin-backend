package com.epam.aidial.ql.deserializers.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.epam.aidial.ql.dto.FilterDto;
import com.epam.aidial.ql.dto.filters.NotDto;

import java.io.IOException;

public class NotDeserializer extends JsonDeserializer<NotDto> {
    @Override
    public NotDto deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        return new NotDto(p.readValueAs(FilterDto.class));
    }
}
