package com.epam.aidial.cfg.domain.model;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CostLimit {
    private BigDecimal minute = BigDecimal.valueOf(Long.MAX_VALUE);
    private BigDecimal day = BigDecimal.valueOf(Long.MAX_VALUE);
    private BigDecimal week = BigDecimal.valueOf(Long.MAX_VALUE);
    private BigDecimal month = BigDecimal.valueOf(Long.MAX_VALUE);
}
