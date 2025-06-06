package com.epam.aidial.metric.model.configuration.influx;

import com.epam.aidial.metric.model.configuration.TableSource;
import lombok.Data;

@Data
public class InfluxTableSource implements TableSource {
    private String bucket;
    private String measurement;
}
