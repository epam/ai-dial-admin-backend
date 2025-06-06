package com.epam.aidial.ql.common.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum ColumnType {
    @JsonProperty("Content")
    CONTENT,
    @JsonProperty("Input Parameters")
    INPUT_PARAMETER,
    @JsonProperty("Run Info")
    RUN_INFO,
    @JsonProperty("Performance Statistic")
    PERFORMANCE_STATISTIC,
    @JsonProperty("Calculated")
    CALCULATED,
    @JsonProperty("Tag")
    TAG
}
