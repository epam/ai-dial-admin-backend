package com.epam.aidial.metric.model.configuration.influx3;

import com.epam.aidial.metric.model.configuration.ColumnDeclaration;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class Influx3ColumnDeclaration extends ColumnDeclaration {
    private Influx3ColumnSource source;
}
