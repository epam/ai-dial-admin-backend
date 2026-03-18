package com.epam.aidial.metric.model.configuration;

import java.util.List;

public abstract class TableSchema {
    public abstract List<? extends ColumnDeclaration> getColumns();
}
