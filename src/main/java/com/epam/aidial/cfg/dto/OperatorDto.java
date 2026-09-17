package com.epam.aidial.cfg.dto;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

@Getter
public enum OperatorDto {

    EQ("=="),
    NE("!="),
    GT(">"),
    LT("<"),
    GE(">="),
    LE("<=");

    @JsonValue
    private final String symbol;

    OperatorDto(String symbol) {
        this.symbol = symbol;
    }
}
