package com.epam.aidial.ql.deserializers.json;

import com.epam.aidial.ql.common.model.enums.SortDirection;
import com.epam.aidial.ql.dto.ExpressionDto;
import com.epam.aidial.ql.dto.SortDto;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ValueDeserializer;

public class SortDeserializer extends ValueDeserializer<SortDto> {
    @Override
    public SortDto deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
        final JsonNode tree = ctxt.readTree(p);
        if (tree.size() == 1) {
            final String fieldName = tree.propertyNames().iterator().next();
            final JsonNode node = tree.get(fieldName);
            switch (fieldName) {
                case "$asc": return new SortDto(ctxt.readTreeAsValue(node, ExpressionDto.class), SortDirection.ASC);
                case "$desc": return new SortDto(ctxt.readTreeAsValue(node, ExpressionDto.class), SortDirection.DESC);
            }
        }
        return ctxt.reportInputMismatch(this, "Sort should contain one property of `$asc` or `$desc`");
    }
}
