package com.epam.aidial.metric.model.configuration;

import lombok.Data;

import java.util.List;

@Data
public abstract class AbstractDatasetDeclaration<
        S extends BaseDataSourceDeclaration,
        T extends TableDeclaration
> implements DatasetDeclaration {
    private String name;
    private String displayedName;
    private String description;
    private S source;
    private List<T> tables;
}
