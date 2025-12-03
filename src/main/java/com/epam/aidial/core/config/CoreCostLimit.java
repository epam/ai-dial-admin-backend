package com.epam.aidial.core.config;

import com.epam.aidial.core.config.databind.OmitBigDecimalValueOfLongMaxValueFilter;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CoreCostLimit {
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OmitBigDecimalValueOfLongMaxValueFilter.class)
    private BigDecimal minute = BigDecimal.valueOf(Long.MAX_VALUE);
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OmitBigDecimalValueOfLongMaxValueFilter.class)
    private BigDecimal day = BigDecimal.valueOf(Long.MAX_VALUE);
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OmitBigDecimalValueOfLongMaxValueFilter.class)
    private BigDecimal week = BigDecimal.valueOf(Long.MAX_VALUE);
    @JsonInclude(value = JsonInclude.Include.CUSTOM, valueFilter = OmitBigDecimalValueOfLongMaxValueFilter.class)
    private BigDecimal month = BigDecimal.valueOf(Long.MAX_VALUE);
}
