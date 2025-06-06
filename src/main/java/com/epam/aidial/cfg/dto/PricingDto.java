package com.epam.aidial.cfg.dto;

import com.epam.aidial.core.config.databind.DoubleStringDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
public class PricingDto {
    private String unit;

    @JsonDeserialize(using = DoubleStringDeserializer.class)
    private String prompt;

    @JsonDeserialize(using = DoubleStringDeserializer.class)
    private String completion;
}