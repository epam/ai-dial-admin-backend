package com.epam.aidial.metric.model.configuration.influx;

import com.epam.aidial.metric.model.configuration.TableSchema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class InfluxTableSchema extends TableSchema {
    private List<InfluxColumnDeclaration> columns;
}
