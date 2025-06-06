package com.epam.aidial.metric.model.configuration;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "$type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = StaticTableSchema.class, name = "static"),
        @JsonSubTypes.Type(value = DynamicTableSchema.class, name = "dynamic"),
})
public interface TableSchema {
}
