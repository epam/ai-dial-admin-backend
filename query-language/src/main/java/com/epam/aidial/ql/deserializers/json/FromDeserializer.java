package com.epam.aidial.ql.deserializers.json;

import com.epam.aidial.ql.dto.CompletableDto;
import com.epam.aidial.ql.dto.FromDto;
import com.epam.aidial.ql.dto.TableDto;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.node.ValueNode;

import java.io.IOException;

public class FromDeserializer extends JsonDeserializer<FromDto> {
    @Override
    public FromDto deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final TreeNode tree = p.getCodec().readTree(p);
        if (tree.isValueNode()) {
            return new TableDto(((ValueNode) tree).asText());
        }
        return tree.traverse(p.getCodec()).readValueAs(CompletableDto.class);
    }
}
