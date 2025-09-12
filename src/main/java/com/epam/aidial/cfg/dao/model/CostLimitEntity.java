package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Embeddable
public class CostLimitEntity {

    @Column(name = "cost_limit_minute")
    private BigDecimal minute;
    @Column(name = "cost_limit_day")
    private BigDecimal day;
    @Column(name = "cost_limit_week")
    private BigDecimal week;
    @Column(name = "cost_limit_month")
    private BigDecimal month;
}
