package com.epam.aidial.metric.model.configuration.influx3;

import com.epam.aidial.metric.model.configuration.BaseDataSourceDeclaration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Influx3DataSourceDeclaration extends BaseDataSourceDeclaration {
    private String database;
}
