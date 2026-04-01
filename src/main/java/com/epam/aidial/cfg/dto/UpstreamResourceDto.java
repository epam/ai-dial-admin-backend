package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpstreamResourceDto {

    private String endpoint;
    private String responsesEndpoint;
    private String key;
    private String extraData;
    private int weight;
    @Min(value = 0, message = "Tier must be a non-negative number")
    private int tier;
}