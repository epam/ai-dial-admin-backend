package com.epam.aidial.metric.model.influx;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor(staticName = "of")
public class FluxQueryPart {
    private final Set<String> imports;
    private final String query;

    private static final FluxQueryPart EMPTY_PART = new FluxQueryPart(Set.of(), "");

    public static FluxQueryPart of() {
        return EMPTY_PART;
    }

    public static FluxQueryPart of(String query) {
        return new FluxQueryPart(Set.of(), query);
    }

}
