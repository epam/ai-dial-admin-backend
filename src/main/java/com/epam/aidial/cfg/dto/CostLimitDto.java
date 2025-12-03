package com.epam.aidial.cfg.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CostLimitDto {

    @PositiveOrZero
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal minute = BigDecimal.valueOf(Long.MAX_VALUE);
    @PositiveOrZero
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal day = BigDecimal.valueOf(Long.MAX_VALUE);
    @PositiveOrZero
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal week = BigDecimal.valueOf(Long.MAX_VALUE);
    @PositiveOrZero
    @JsonSerialize(using = ToStringSerializer.class)
    private BigDecimal month = BigDecimal.valueOf(Long.MAX_VALUE);
}
