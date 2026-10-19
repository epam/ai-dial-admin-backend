package com.epam.aidial.cfg.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ModelCapabilitiesDataDto {

    private List<String> scaleTypes;
    private Boolean completion;
    private Boolean chatCompletion;
    private Boolean embeddings;
    private Boolean fineTune;
    private Boolean inference;
}