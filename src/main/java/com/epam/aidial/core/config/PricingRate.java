package com.epam.aidial.core.config;

import com.epam.aidial.core.config.databind.PricingRateDeserializer;
import com.epam.aidial.core.config.databind.PricingRateSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

@Data
@JsonDeserialize(using = PricingRateDeserializer.class)
@JsonSerialize(using = PricingRateSerializer.class)
public class PricingRate {

    // leaf shape
    private String rate;

    // node shape; ifTrue/ifFalse omitted -> falls back to the prompt rate
    private Condition test;
    private PricingRate ifTrue;
    private PricingRate ifFalse;

    public boolean isLeaf() {
        return rate != null;
    }
}
