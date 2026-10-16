package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.databind.PricingRateDtoDeserializer;
import com.epam.aidial.cfg.dto.databind.PricingRateDtoSerializer;
import lombok.Data;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

@Data
@JsonDeserialize(using = PricingRateDtoDeserializer.class)
@JsonSerialize(using = PricingRateDtoSerializer.class)
public class PricingRateDto {

    // leaf shape
    private String rate;

    // node shape; ifTrue/ifFalse omitted -> falls back to the prompt rate
    private PricingRateConditionDto test;
    private PricingRateDto ifTrue;
    private PricingRateDto ifFalse;

    public static PricingRateDto flat(String rate) {
        PricingRateDto pricingRate = new PricingRateDto();
        pricingRate.setRate(rate);
        return pricingRate;
    }

    public boolean isLeaf() {
        return rate != null;
    }
}
