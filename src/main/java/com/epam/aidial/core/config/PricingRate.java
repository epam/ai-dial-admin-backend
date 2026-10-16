package com.epam.aidial.core.config;

import com.epam.aidial.core.config.databind.PricingRateDeserializer;
import com.epam.aidial.core.config.databind.PricingRateSerializer;
import lombok.Data;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@Data
@JsonDeserialize(using = PricingRateDeserializer.class)
@JsonSerialize(using = PricingRateSerializer.class)
public class PricingRate {

    // leaf shape
    private String rate;

    // node shape; ifTrue/ifFalse omitted -> falls back to the prompt rate
    private PricingRateCondition test;
    private PricingRate ifTrue;
    private PricingRate ifFalse;

    public static PricingRate flat(String rate) {
        PricingRate pricingRate = new PricingRate();
        pricingRate.setRate(rate);
        return pricingRate;
    }

    public boolean isLeaf() {
        return rate != null;
    }
}
