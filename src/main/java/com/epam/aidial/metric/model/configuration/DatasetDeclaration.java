package com.epam.aidial.metric.model.configuration;

import com.epam.aidial.metric.model.configuration.influx.InfluxDatasetDeclaration;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonSubTypes.Type;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "$type")
@JsonSubTypes({
        @Type(value = InfluxDatasetDeclaration.class, name = "influx"),
})
public interface DatasetDeclaration {

    String getName();

    String getDisplayedName();

    String getDescription();

    BaseDataSourceDeclaration getSource();

    List<? extends TableDeclaration> getTables();

}
