package com.epam.aidial.metric.service.influx3;

import com.epam.aidial.metric.component.TemporalNameGenerator;
import com.epam.aidial.metric.config.Influx3DatasetConfiguration;
import com.epam.aidial.metric.model.configuration.influx3.Influx3DatasetDeclaration;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SqlQueryBuilderFactory {
    private final Influx3DatasetDeclaration datasetDeclaration;
    private final Influx3DatasetConfiguration datasourceConfiguration;

    public SqlQueryBuilder createQueryBuilder() {
        return new SqlQueryBuilder(datasetDeclaration, datasourceConfiguration, new TemporalNameGenerator());
    }
}
