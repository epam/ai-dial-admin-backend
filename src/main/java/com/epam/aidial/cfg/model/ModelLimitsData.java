package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ModelLimitsData {

    private Integer maxTotalTokens;
    private Integer maxPromptTokens;
    private Integer maxCompletionTokens;
}
