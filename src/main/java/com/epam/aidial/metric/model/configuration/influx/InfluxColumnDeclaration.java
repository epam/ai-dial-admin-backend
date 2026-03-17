package com.epam.aidial.metric.model.configuration.influx;

import com.epam.aidial.metric.model.configuration.ColumnDeclaration;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class InfluxColumnDeclaration extends ColumnDeclaration {
    private InfluxColumnSource source;
}
