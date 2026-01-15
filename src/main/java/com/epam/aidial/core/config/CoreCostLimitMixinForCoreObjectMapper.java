package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

public abstract class CoreCostLimitMixinForCoreObjectMapper {
    @JsonInclude
    private BigDecimal minute;
    @JsonInclude
    private BigDecimal day;
    @JsonInclude
    private BigDecimal week;
    @JsonInclude
    private BigDecimal month;
}
