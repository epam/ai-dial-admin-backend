package com.epam.aidial.metric.model.configuration;

import lombok.Data;

import java.util.List;

@Data
public class StaticTableSchema implements TableSchema {
    private List<ColumnDeclaration> columns;
}
