package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ConditionDto {

    @NotNull
    private String field;

    @NotNull
    private OperatorDto operator;

    @NotNull
    private Object value;
}
