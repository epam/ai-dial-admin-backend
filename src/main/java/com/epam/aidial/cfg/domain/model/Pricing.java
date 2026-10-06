package com.epam.aidial.cfg.domain.model;

import lombok.Data;

@Data
public class Pricing {
    private String unit;
    private PricingRate prompt;
    private PricingRate completion;
    private PricingRate cacheRead;
    private PricingRate cacheWrite;
}