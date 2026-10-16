package com.epam.aidial.ql.deserializers.json;

import com.epam.aidial.ql.dto.CompletableDto;
import com.epam.aidial.ql.dto.FromDto;
import com.epam.aidial.ql.dto.TableDto;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class FromDeserializer extends ValueDeserializer<FromDto> {
    @Override
    public FromDto deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        final JsonNode tree = ctxt.readTree(p);
        if (tree.isValueNode()) {
            return new TableDto(tree.asString());
        }
        return ctxt.readTreeAsValue(tree, CompletableDto.class);
    }
}
