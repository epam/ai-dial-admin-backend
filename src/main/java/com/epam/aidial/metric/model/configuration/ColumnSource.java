package com.epam.aidial.metric.model.configuration;

import com.epam.aidial.metric.model.configuration.influx.InfluxColumnSource;
import com.epam.aidial.metric.model.configuration.influx3.Influx3ColumnSource;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, defaultImpl = Influx3ColumnSource.class)
@JsonSubTypes({
        @JsonSubTypes.Type(value = InfluxColumnSource.class),
        @JsonSubTypes.Type(value = Influx3ColumnSource.class),
})
public interface ColumnSource {
}
