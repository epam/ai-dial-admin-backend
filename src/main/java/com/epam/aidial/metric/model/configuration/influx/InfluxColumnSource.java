package com.epam.aidial.metric.model.configuration.influx;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfluxColumnSource {
    private String column;
    private InfluxColumnSourceType type;
}
