package com.epam.aidial.metric.config;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@EqualsAndHashCode(callSuper = true)
@Component
@ConfigurationProperties(prefix = "metrics.influx3")
public class Influx3DatasetConfiguration extends AbstractDatasetConfiguration {
}
