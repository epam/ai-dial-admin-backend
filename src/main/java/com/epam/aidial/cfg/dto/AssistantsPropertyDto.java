package com.epam.aidial.cfg.dto;

import lombok.Data;

@Data
public class AssistantsPropertyDto {

    private String endpoint;
    private FeaturesDto features = new FeaturesDto();
}
