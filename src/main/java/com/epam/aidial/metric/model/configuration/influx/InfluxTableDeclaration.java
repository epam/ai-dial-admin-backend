package com.epam.aidial.metric.model.configuration.influx;

import com.epam.aidial.metric.model.configuration.TableDeclaration;
import com.epam.aidial.metric.model.configuration.TableSchema;
import lombok.Data;

@Data
public class InfluxTableDeclaration implements TableDeclaration {
    private String name;
    private InfluxTableSource source;
    private TableSchema schema;
}
