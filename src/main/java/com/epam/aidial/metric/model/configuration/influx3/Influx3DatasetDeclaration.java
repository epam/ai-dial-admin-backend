package com.epam.aidial.metric.model.configuration.influx3;

import com.epam.aidial.metric.model.configuration.AbstractDatasetDeclaration;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Influx3DatasetDeclaration extends AbstractDatasetDeclaration<Influx3DataSourceDeclaration, Influx3TableDeclaration> {
}
