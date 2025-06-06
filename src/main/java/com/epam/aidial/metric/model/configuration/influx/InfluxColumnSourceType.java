package com.epam.aidial.metric.model.configuration.influx;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.commons.lang3.EnumUtils;

public enum InfluxColumnSourceType {
    TAG,
    FIELD,
    ;

    @JsonCreator
    public static InfluxColumnSourceType getByValue(String value) {
        return EnumUtils.getEnumIgnoreCase(InfluxColumnSourceType.class, value);
    }

}
