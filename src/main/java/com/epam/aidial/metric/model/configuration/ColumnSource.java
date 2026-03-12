package com.epam.aidial.metric.model.configuration;

import com.epam.aidial.metric.model.configuration.influx.InfluxColumnSource;
import com.epam.aidial.metric.model.configuration.influx3.Influx3ColumnSource;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "$type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = InfluxColumnSource.class, name = "influx2"),
        @JsonSubTypes.Type(value = Influx3ColumnSource.class, name = "influx3"),
})
public interface ColumnSource {
}
