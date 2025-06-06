package com.epam.aidial.ql.deserializers.json;

import com.epam.aidial.ql.dto.CompletableDto;
import com.epam.aidial.ql.dto.ExpressionDto;
import com.epam.aidial.ql.dto.StringExpressionDto;
import com.epam.aidial.ql.dto.TupleDto;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ValueNode;

import java.io.IOException;

public class ExpressionDeserializer extends JsonDeserializer<ExpressionDto> {
    @Override
    public ExpressionDto deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final TreeNode tree = p.getCodec().readTree(p);
        if (tree.isValueNode()) {
            return new StringExpressionDto(((ValueNode) tree).asText());
        } else if (tree.isObject()) {
            return tree.traverse(p.getCodec()).readValueAs(CompletableDto.class);
        } else {
            return tree.traverse(p.getCodec()).readValueAs(TupleDto.class);
        }
    }
}
