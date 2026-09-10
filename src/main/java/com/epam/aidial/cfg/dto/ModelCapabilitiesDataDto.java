package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.List;

@Data
public class ModelCapabilitiesDataDto {

    private List<String> scaleTypes;
    private Boolean completion;
    private Boolean chatCompletion;
    private Boolean embeddings;
    private Boolean fineTune;
    private Boolean inference;
}
