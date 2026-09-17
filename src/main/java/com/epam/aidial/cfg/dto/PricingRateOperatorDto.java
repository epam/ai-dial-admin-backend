package com.epam.aidial.cfg.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum PricingRateOperatorDto {

    EQ("=="),
    NE("!="),
    GT(">"),
    LT("<"),
    GE(">="),
    LE("<=");

    @JsonValue
    private final String symbol;

    PricingRateOperatorDto(String symbol) {
        this.symbol = symbol;
    }
}
