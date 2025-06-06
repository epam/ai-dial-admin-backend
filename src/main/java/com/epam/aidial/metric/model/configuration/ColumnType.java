package com.epam.aidial.metric.model.configuration;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.apache.commons.lang3.EnumUtils;

public enum ColumnType {
    STRING,
    TIMESTAMP,
    DOUBLE,
    INT64,
    UINT64,
    ;

    @JsonCreator
    public static ColumnType getByValue(String value) {
        return EnumUtils.getEnumIgnoreCase(ColumnType.class, value);
    }

}
