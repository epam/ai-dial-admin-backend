package com.epam.aidial.expressions.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Type of a report property.
 */
public enum Type {
    /**
     * Boolean type.
     */
    @JsonProperty("Boolean")
    BOOLEAN,
    /**
     * Integer type (8-bit).
     */
    @JsonProperty("Int8")
    INT_8,
    /**
     * Unsigned integer type (8-bit).
     */
    @JsonProperty("UInt8")
    UINT_8,
    /**
     * Integer type (16-bit).
     */
    @JsonProperty("Int16")
    INT_16,
    /**
     * Unsigned integer type (16-bit).
     */
    @JsonProperty("UInt16")
    UINT_16,
    /**
     * Integer type (32-bit).
     */
    @JsonProperty("Int32")
    INT_32,
    /**
     * Unsigned integer type (32-bit).
     */
    @JsonProperty("UInt32")
    UINT_32,
    /**
     * Integer type (64-bit).
     */
    @JsonProperty("Int64")
    INT_64,
    /**
     * Unsigned integer type (64-bit).
     */
    @JsonProperty("UInt64")
    UINT_64,
    /**
     * Float type.
     */
    @JsonProperty("Float")
    FLOAT,
    /**
     * Double type.
     */
    @JsonProperty("Double")
    DOUBLE,
    /**
     * Enum type.
     */
    @JsonProperty("Enum")
    ENUM,
    /**
     * Char type.
     */
    @JsonProperty("Char")
    CHAR,
    /**
     * String type.
     */
    @JsonProperty("String")
    STRING,
    /**
     * Timestamp type. Defines a moment of time.
     */
    @JsonProperty("Timestamp")
    TIMESTAMP,
    /**
     * Interval type. Defines time interval.
     */
    @JsonProperty("Interval")
    INTERVAL,
    /**
     * UUID type.
     */
    @JsonProperty("Uuid")
    UUID,

    @JsonProperty("Nothing")
    NOTHING,

    @JsonProperty("Tuple")
    TUPLE,

    @JsonProperty("Binary")
    BINARY,

    // TODO Move it only in TypeDto
    @JsonProperty("Tag")
    TAG
}
