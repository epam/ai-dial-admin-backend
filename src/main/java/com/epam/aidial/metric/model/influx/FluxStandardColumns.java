package com.epam.aidial.metric.model.influx;

import lombok.experimental.UtilityClass;

@UtilityClass
public class FluxStandardColumns {
    public static final String FIELD_COLUMN = "_field";
    public static final String MEASUREMENT_COLUMN = "_measurement";
    public static final String START_COLUMN = "_start";
    public static final String STOP_COLUMN = "_stop";
    public static final String TIME_COLUMN = "_time";
    public static final String VALUE_COLUMN = "_value";
}
