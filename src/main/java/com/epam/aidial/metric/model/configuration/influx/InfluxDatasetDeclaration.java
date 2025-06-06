package com.epam.aidial.metric.model.configuration.influx;

import com.epam.aidial.metric.model.configuration.DatasetDeclaration;
import lombok.Data;

import java.util.List;

@Data
public class InfluxDatasetDeclaration implements DatasetDeclaration {
    private String name;
    private String displayedName;
    private String description;
    private InfluxDataSourceDeclaration source;
    private List<InfluxTableDeclaration> tables;
}
