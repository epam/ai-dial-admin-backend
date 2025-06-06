package com.epam.aidial.ql.common.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Sort direction.
 */
public enum SortDirection {
    /**
     * Ascending sort.
     */
    @JsonProperty("$asc")
    ASC,
    /**
     * Descending sort.
     */
    @JsonProperty("$desc")
    DESC
}
