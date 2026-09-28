package com.epam.aidial.core.config;

import com.epam.aidial.core.config.databind.DoubleStringDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
public class Pricing {
    private String unit;

    @JsonDeserialize(using = DoubleStringDeserializer.class)
    private String prompt;

    @JsonDeserialize(using = DoubleStringDeserializer.class)
    private String completion;

    private PricingRate cacheRead; // 0.47.0, type changed to PricingRate in 0.48.0

    private PricingRate cacheWrite; // 0.47.0, type changed to PricingRate in 0.48.0
}