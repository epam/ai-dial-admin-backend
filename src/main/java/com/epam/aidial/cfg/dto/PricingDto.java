package com.epam.aidial.cfg.dto;

import lombok.Data;

@Data
public class PricingDto {
    private String unit;

    private PricingRateDto prompt;

    private PricingRateDto completion;

    private PricingRateDto cacheRead;

    private PricingRateDto cacheWrite;
}