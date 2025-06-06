package com.epam.aidial.ql.common.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum DenialCause {
    @JsonProperty("Type")
    TYPE,
    @JsonProperty("NotDeterministic")
    NOT_DETERMINISTIC,
    @JsonProperty("Aggregation")
    AGGREGATION,
    @JsonProperty("Scale")
    SCALE
}
