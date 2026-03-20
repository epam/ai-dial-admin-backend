package com.epam.aidial.metric.model.influx;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FluxStandardImports {
    public static final String SCHEMA = "import \"influxdata/influxdb/schema\"";
    public static final String STRINGS = "import \"strings\"";
    public static final String REGEXP = "import \"regexp\"";
}
