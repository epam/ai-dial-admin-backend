package com.epam.aidial.metric.model.configuration.influx3;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Influx3ColumnSource {
    private String column;
}
