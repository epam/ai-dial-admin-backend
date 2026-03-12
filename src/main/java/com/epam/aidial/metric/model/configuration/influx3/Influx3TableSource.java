package com.epam.aidial.metric.model.configuration.influx3;

import com.epam.aidial.metric.model.configuration.TableSource;
import lombok.Data;

@Data
public class Influx3TableSource implements TableSource {
    private String table;
}
