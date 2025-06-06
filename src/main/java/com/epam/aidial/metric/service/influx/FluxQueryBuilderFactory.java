package com.epam.aidial.metric.service.influx;

import com.epam.aidial.metric.component.TemporalNameGenerator;
import com.epam.aidial.metric.config.InfluxDatasetConfiguration;
import com.epam.aidial.metric.model.configuration.influx.InfluxDatasetDeclaration;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FluxQueryBuilderFactory {
    private final InfluxDatasetDeclaration datasetDeclaration;
    private final InfluxDatasetConfiguration datasourceConfiguration;

    public FluxQueryBuilder createQueryBuilder() {
        return new FluxQueryBuilder(datasetDeclaration, datasourceConfiguration, new TemporalNameGenerator());
    }

}
