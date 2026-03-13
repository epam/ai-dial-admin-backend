package com.epam.aidial.metric.model.configuration.influx3;

import com.epam.aidial.metric.model.configuration.AbstractTableDeclaration;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Influx3TableDeclaration extends AbstractTableDeclaration<Influx3TableSource> {
}
