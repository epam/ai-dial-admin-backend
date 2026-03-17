package com.epam.aidial.metric.model.configuration;

import lombok.Data;

@Data
public abstract class ColumnDeclaration {
    private String name;
    private ColumnType type;
}
