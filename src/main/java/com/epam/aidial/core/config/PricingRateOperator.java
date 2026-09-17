package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum PricingRateOperator {

    EQ("=="),
    NE("!="),
    GT(">"),
    LT("<"),
    GE(">="),
    LE("<=");

    @JsonValue
    private final String symbol;

    PricingRateOperator(String symbol) {
        this.symbol = symbol;
    }
}
