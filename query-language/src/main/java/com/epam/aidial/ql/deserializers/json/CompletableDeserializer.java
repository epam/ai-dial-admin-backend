package com.epam.aidial.ql.deserializers.json;

import com.epam.aidial.ql.dto.CompletableDto;
import com.epam.aidial.ql.dto.QueryDto;
import com.epam.aidial.ql.dto.UnionAllDto;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class CompletableDeserializer extends ValueDeserializer<CompletableDto> {
    @Override
    public CompletableDto deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        final JsonNode tree = ctxt.readTree(p);
        if (tree.get("unionAll") != null) {
            return ctxt.readTreeAsValue(tree, UnionAllDto.class);
        } else {
            return ctxt.readTreeAsValue(tree, QueryDto.class);
        }
    }
}
