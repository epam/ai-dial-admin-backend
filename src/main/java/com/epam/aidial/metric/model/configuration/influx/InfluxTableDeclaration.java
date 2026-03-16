package com.epam.aidial.metric.model.configuration.influx;

import com.epam.aidial.metric.model.configuration.AbstractTableDeclaration;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class InfluxTableDeclaration extends AbstractTableDeclaration<InfluxTableSource> {
    private InfluxTableSchema schema;
}
