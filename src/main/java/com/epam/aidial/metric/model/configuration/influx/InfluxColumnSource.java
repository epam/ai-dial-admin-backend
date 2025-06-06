package com.epam.aidial.metric.model.configuration.influx;

import com.epam.aidial.metric.model.configuration.ColumnSource;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InfluxColumnSource implements ColumnSource {
    private String column;
    private InfluxColumnSourceType type;
}
