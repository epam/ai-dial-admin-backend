package com.epam.aidial.metric.service.influx;

import com.epam.aidial.metric.component.TemporalNameGenerator;
import com.epam.aidial.metric.model.configuration.influx.InfluxDatasetDeclaration;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FluxQueryBuilderFactory {
    private final InfluxDatasetDeclaration datasetDeclaration;

    public FluxQueryBuilder createQueryBuilder() {
        return new FluxQueryBuilder(datasetDeclaration, new TemporalNameGenerator());
    }

}
