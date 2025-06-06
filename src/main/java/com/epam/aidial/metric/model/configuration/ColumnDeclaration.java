package com.epam.aidial.metric.model.configuration;

import lombok.Data;

@Data
public class ColumnDeclaration {
    private String name;
    private ColumnType type;
    private ColumnSource source;
}
