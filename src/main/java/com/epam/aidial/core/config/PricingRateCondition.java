package com.epam.aidial.core.config;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PricingRateCondition {

    @NotNull
    private String field;

    @NotNull
    private PricingRateOperator operator;

    @NotNull
    private Object value;
}
