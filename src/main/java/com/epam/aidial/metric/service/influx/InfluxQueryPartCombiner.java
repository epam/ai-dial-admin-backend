package com.epam.aidial.metric.service.influx;

import com.epam.aidial.metric.model.influx.FluxQueryPart;
import org.apache.commons.lang3.StringUtils;

import java.util.HashSet;
import java.util.Set;

public class InfluxQueryPartCombiner {

    private final Set<String> imports = new HashSet<>();
    private final StringBuilder builder = new StringBuilder();

    public InfluxQueryPartCombiner add(FluxQueryPart part) {
        add(part.getImports());
        add(part.getQuery());
        return this;
    }

    public InfluxQueryPartCombiner add(String queryPart, Set<String> imports) {
        add(queryPart);
        add(imports);
        return this;
    }

    public InfluxQueryPartCombiner add(String queryPart) {
        if (!StringUtils.isEmpty(queryPart)) {
            if (!builder.isEmpty()) {
                builder.append("\n");
            }
            builder.append(queryPart);
        }
        return this;
    }

    public InfluxQueryPartCombiner add(Set<String> imports) {
        this.imports.addAll(imports);
        return this;
    }

    public FluxQueryPart build() {
        return FluxQueryPart.of(imports, builder.toString());
    }

}
