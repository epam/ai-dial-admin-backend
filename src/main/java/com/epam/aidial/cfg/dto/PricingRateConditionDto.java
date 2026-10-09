package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PricingRateConditionDto {

    @NotNull
    private String field;

    @NotNull
    private PricingRateOperatorDto operator;

    @NotNull
    private Object value;
}
