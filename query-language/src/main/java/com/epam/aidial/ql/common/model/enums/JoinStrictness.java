package com.epam.aidial.ql.common.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum JoinStrictness {
    @JsonProperty("Any")
    ANY,
    @JsonProperty("All")
    ALL
}
