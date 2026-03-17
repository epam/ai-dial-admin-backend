package com.epam.aidial.metric.model.influx3;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
@Builder
public class SqlQueryContext {
    private final String query;
    private final List<String> columnNames;
    private final Map<String, Object> parameters;
}
