package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum Operator {

    EQ("=="),
    NE("!="),
    GT(">"),
    LT("<"),
    GE(">="),
    LE("<=");

    @JsonValue
    private final String symbol;

    Operator(String symbol) {
        this.symbol = symbol;
    }
}
