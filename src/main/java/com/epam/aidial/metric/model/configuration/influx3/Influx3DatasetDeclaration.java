package com.epam.aidial.metric.model.configuration.influx3;

import com.epam.aidial.metric.model.configuration.DatasetDeclaration;
import lombok.Data;

import java.util.List;

@Data
public class Influx3DatasetDeclaration implements DatasetDeclaration {
    private String name;
    private String displayedName;
    private String description;
    private Influx3DataSourceDeclaration source;
    private List<Influx3TableDeclaration> tables;
}
