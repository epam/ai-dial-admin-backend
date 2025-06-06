package com.epam.aidial.metric.model.configuration;

import lombok.Data;

import java.util.List;

@Data
public class DatasetsConfiguration {
    private List<DatasetDeclaration> datasets;
}
