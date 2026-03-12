package com.epam.aidial.metric.model.configuration.influx3;

import com.epam.aidial.metric.model.configuration.TableDeclaration;
import com.epam.aidial.metric.model.configuration.TableSchema;
import lombok.Data;

@Data
public class Influx3TableDeclaration implements TableDeclaration {
    private String name;
    private Influx3TableSource source;
    private TableSchema schema;
}
