package com.epam.aidial.metric.model.configuration.influx3;

import com.epam.aidial.metric.model.configuration.TableSchema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class Influx3TableSchema extends TableSchema {
    private List<Influx3ColumnDeclaration> columns;
}
