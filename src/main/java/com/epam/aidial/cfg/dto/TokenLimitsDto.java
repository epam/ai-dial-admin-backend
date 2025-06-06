package com.epam.aidial.cfg.dto;

import lombok.Data;

@Data
public class TokenLimitsDto {
    private Integer maxTotalTokens;
    private Integer maxPromptTokens;
    private Integer maxCompletionTokens;
}