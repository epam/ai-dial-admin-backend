package com.epam.aidial.ql.common.model.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum BinaryComparisonOperator {
    /**
     Equals operator (=).
     */
    @JsonProperty("$eq")
    EQUALS,

    /**
     Mot equals operator (<>).
     */
    @JsonProperty("$ne")
    NOT_EQUALS,

    /**
     Less operator (<).
     */
    @JsonProperty("$lt")
    LESS,

    /**
     Less or equals operator (<=).
     */
    @JsonProperty("$lte")
    LESS_OR_EQUALS,

    /**
     Greater operator (>).
     */
    @JsonProperty("$gt")
    GREATER,

    /**
     Greater or equals operator (>=).
     */
    @JsonProperty("$gte")
    GREATER_OR_EQUALS,

    /**
     Pattern matching operator (LIKE).
     */
    @JsonProperty("$like")
    LIKE,

    /**
     Pattern matching operator (LIKE).
     */
    @JsonProperty("$not_like")
    NOT_LIKE,

    /**
     Pattern matching operator (CONTAINS).
     */
    @JsonProperty("$contains")
    CONTAINS,

    /**
     Pattern matching operator (NOT_CONTAINS).
     */
    @JsonProperty("$not_contains")
    NOT_CONTAINS,

    /**
     Pattern matching operator (STARTS_WITH).
     */
    @JsonProperty("$starts_with")
    STARTS_WITH,

    /**
     Pattern matching operator (ENDS_WITH).
     */
    @JsonProperty("$ends_with")
    ENDS_WITH,

    /**
     Set matching operator (IN).
     */
    @JsonProperty("$in")
    IN,

    /**
     * Set matching operator (NOT IN).
     */
    @JsonProperty("$nin")
    NOT_IN
}
