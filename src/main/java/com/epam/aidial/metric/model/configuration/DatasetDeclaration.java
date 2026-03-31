package com.epam.aidial.metric.model.configuration;

import com.epam.aidial.metric.model.configuration.influx.InfluxDatasetDeclaration;
import com.epam.aidial.metric.model.configuration.influx3.Influx3DatasetDeclaration;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.time.Duration;
import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "$type")
@JsonSubTypes({
        @Type(value = InfluxDatasetDeclaration.class, names = {"influx", "influx2"}),
        @Type(value = Influx3DatasetDeclaration.class, name = "influx3"),
})
public interface DatasetDeclaration {

    String getName();

    String getDisplayedName();

    String getDescription();

    BaseDataSourceDeclaration getSource();

    List<? extends TableDeclaration> getTables();

    Duration getMaxTimeRange();

}
