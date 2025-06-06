package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class TokenLimitsEntity {
    private Integer maxTotalTokens;
    private Integer maxPromptTokens;
    private Integer maxCompletionTokens;
}