package com.epam.aidial.metric.model.configuration;

import lombok.Data;

@Data
public abstract class AbstractTableDeclaration<S extends TableSource> implements TableDeclaration {
    private String name;
    private S source;
    private TableSchema schema;
}
