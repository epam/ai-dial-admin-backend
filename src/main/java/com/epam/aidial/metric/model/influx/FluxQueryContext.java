package com.epam.aidial.metric.model.influx;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class FluxQueryContext {
    private final Set<String> imports;
    @Builder.Default
    private final List<String> preamble = List.of();
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
