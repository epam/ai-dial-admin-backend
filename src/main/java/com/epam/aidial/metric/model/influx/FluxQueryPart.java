package com.epam.aidial.metric.model.influx;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@AllArgsConstructor
public class FluxQueryPart {
    private final Set<String> imports;
    private final List<String> preamble;
    private final String query;

    private static final FluxQueryPart EMPTY_PART = new FluxQueryPart(Set.of(), List.of(), "");

    public static FluxQueryPart of() {
        return EMPTY_PART;
    }

    public static FluxQueryPart of(String query) {
        return new FluxQueryPart(Set.of(), List.of(), query);
    }

    public static FluxQueryPart of(Set<String> imports, String query) {
        return new FluxQueryPart(imports, List.of(), query);
    }

    public static FluxQueryPart of(Set<String> imports, List<String> preamble, String query) {
        return new FluxQueryPart(imports, preamble, query);
    }

}
