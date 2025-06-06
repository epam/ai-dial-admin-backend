package com.epam.aidial.metric.model.configuration;

import com.epam.aidial.metric.model.configuration.influx.InfluxColumnSource;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "$type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = InfluxColumnSource.class, name = "influx"),
})
public interface ColumnSource {
}
