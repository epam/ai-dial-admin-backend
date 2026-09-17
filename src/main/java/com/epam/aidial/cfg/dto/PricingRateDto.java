package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.databind.PricingRateDtoDeserializer;
import com.epam.aidial.cfg.dto.databind.PricingRateDtoSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

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

    public boolean isLeaf() {
        return rate != null;
    }
}
