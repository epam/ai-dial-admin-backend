package com.epam.aidial.cfg.domain.model;

import lombok.Data;

@Data
public class TokenLimits {
    private Integer maxTotalTokens;
    private Integer maxPromptTokens;
    private Integer maxCompletionTokens;
}