package com.epam.aidial.ql.deserializers.json;

import com.epam.aidial.ql.dto.CompletableDto;
import com.epam.aidial.ql.dto.ExpressionDto;
import com.epam.aidial.ql.dto.StringExpressionDto;
import com.epam.aidial.ql.dto.TupleDto;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class ExpressionDeserializer extends ValueDeserializer<ExpressionDto> {
    @Override
    public ExpressionDto deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        final JsonNode tree = ctxt.readTree(p);
        if (tree.isValueNode()) {
            return new StringExpressionDto(tree.asString());
        } else if (tree.isObject()) {
            return ctxt.readTreeAsValue(tree, CompletableDto.class);
        } else {
            return ctxt.readTreeAsValue(tree, TupleDto.class);
        }
    }
}
