package com.epam.aidial.ql.deserializers.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.TreeNode;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.PropertyBindingException;
import com.epam.aidial.ql.common.model.enums.SortDirection;
import com.epam.aidial.ql.dto.ExpressionDto;
import com.epam.aidial.ql.dto.SortDto;

import java.io.IOException;

public class SortDeserializer extends JsonDeserializer<SortDto> {
    @Override
    public SortDto deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
        final TreeNode tree = p.getCodec().readTree(p);
        if (tree.size() == 1) {
            final String fieldName = tree.fieldNames().next();
            final JsonParser parser = tree.get(fieldName).traverse(p.getCodec());
            switch (fieldName) {
                case "$asc": return new SortDto(parser.readValueAs(ExpressionDto.class), SortDirection.ASC);
                case "$desc": return new SortDto(parser.readValueAs(ExpressionDto.class), SortDirection.DESC);
            }
        }
        throw PropertyBindingException.from(p, "Sort should contain one property of `$asc` or `$desc`");
    }
}
