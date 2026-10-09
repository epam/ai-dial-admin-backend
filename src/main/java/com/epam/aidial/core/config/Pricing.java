package com.epam.aidial.core.config;

import lombok.Data;

@Data
public class Pricing {
    private String unit;

    private PricingRate prompt; // type changed to PricingRate in 0.49.0

    private PricingRate completion; // type changed to PricingRate in 0.49.0

    private PricingRate cacheRead; // 0.47.0, type changed to PricingRate in 0.48.0

    private PricingRate cacheWrite; // 0.47.0, type changed to PricingRate in 0.48.0
}