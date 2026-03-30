package com.epam.aidial.metric.model.configuration;

import com.epam.aidial.metric.util.DurationDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.time.Duration;
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

    @JsonDeserialize(using = DurationDeserializer.class)
    private Duration maxTimeRange;
}
