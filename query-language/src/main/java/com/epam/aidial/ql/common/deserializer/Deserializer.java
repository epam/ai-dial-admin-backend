package com.epam.aidial.ql.common.deserializer;

import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.JsonNode;

public interface Deserializer<T> {
    T deserialize(JsonNode node, DeserializationContext ctxt);
}
