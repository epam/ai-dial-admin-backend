package com.epam.aidial.ql.common.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum UnaryComparisonOperator {
    @JsonProperty("$isNull")
    IS_NULL,
    @JsonProperty("$isNotNull")
    IS_NOT_NULL
}
