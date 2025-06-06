package com.epam.aidial.metric.model.configuration.influx;

import com.epam.aidial.metric.model.configuration.BaseDataSourceDeclaration;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class InfluxDataSourceDeclaration extends BaseDataSourceDeclaration {
    private String org;
}
