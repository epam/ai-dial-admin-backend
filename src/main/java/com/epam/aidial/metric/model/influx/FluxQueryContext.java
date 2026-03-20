package com.epam.aidial.metric.model.influx;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class FluxQueryContext {
    private final Set<String> imports;
    private final List<String> preamble;
    private final String query;
    private final List<String> columnNames;

    public String buildFullQuery() {
        var builder = new StringBuilder();
        for (var imp : imports) {
            builder.append(imp).append("\n");
        }
        for (var line : preamble) {
            builder.append(line).append("\n");
        }
        builder.append(query);
        return builder.toString();
    }

}
