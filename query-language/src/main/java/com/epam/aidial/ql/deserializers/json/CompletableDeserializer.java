package com.epam.aidial.ql.deserializers.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.epam.aidial.ql.dto.CompletableDto;
import com.epam.aidial.ql.dto.QueryDto;
import com.epam.aidial.ql.dto.UnionAllDto;

import java.io.IOException;

public class CompletableDeserializer extends JsonDeserializer<CompletableDto> {
    @Override
    public CompletableDto deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final TreeNode tree = p.getCodec().readTree(p);
        if (tree.get("unionAll") != null) {
            return tree.traverse(p.getCodec()).readValueAs(UnionAllDto.class);
        } else {
            return tree.traverse(p.getCodec()).readValueAs(QueryDto.class);
        }
    }
}
