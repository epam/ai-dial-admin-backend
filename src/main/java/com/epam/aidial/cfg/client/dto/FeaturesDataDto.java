package com.epam.aidial.cfg.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class FeaturesDataDto {

    private boolean rate;
    private boolean tokenize;
    private boolean truncatePrompt;
    private boolean configuration;
    private boolean systemPrompt;
    private boolean tools;
    private boolean seed;
    private boolean urlAttachments;
    private boolean folderAttachments;
    private boolean allowResume;
    private boolean accessibleByPerRequestKey;
    private boolean contentParts;
    private boolean temperature;
    private boolean cache;
    private boolean autoCaching;
    private boolean parallelToolCalls;
    private boolean assistantAttachmentsInRequest;
    private boolean mcp;
}