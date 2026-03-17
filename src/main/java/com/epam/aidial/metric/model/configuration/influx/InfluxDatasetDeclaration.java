package com.epam.aidial.metric.model.configuration.influx;

import com.epam.aidial.metric.model.configuration.AbstractDatasetDeclaration;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class InfluxDatasetDeclaration extends AbstractDatasetDeclaration<InfluxDataSourceDeclaration, InfluxTableDeclaration> {
}
