package com.epam.aidial.metric.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "metrics.influx2")
public class InfluxDatasetConfiguration {

    private long defaultPageSize;

}
