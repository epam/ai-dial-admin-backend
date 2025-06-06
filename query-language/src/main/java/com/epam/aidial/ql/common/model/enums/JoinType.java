package com.epam.aidial.ql.common.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum JoinType {
    @JsonProperty("Inner")
    INNER,
    @JsonProperty("Left")
    LEFT
}
