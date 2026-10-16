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
public class FeaturesDto {

    private String rateEndpoint;
    private String tokenizeEndpoint;
    private String truncatePromptEndpoint;
    private String configurationEndpoint;
    private Boolean systemPromptSupported;
    private Boolean toolsSupported;
    private Boolean seedSupported;
    private Boolean urlAttachmentsSupported;
    private Boolean folderAttachmentsSupported;
    private Boolean allowResume;
    private Boolean accessibleByPerRequestKey;
    private Boolean contentPartsSupported;
    private Boolean consentRequired;
    private Boolean temperatureSupported;
    private Boolean cacheSupported;
    private Boolean autoCachingSupported;
    private Boolean parallelToolCallsSupported;
    private Boolean assistantAttachmentsInRequestSupported;
    private Boolean supportCommentInRateResponse;
    private Boolean maxTokensSupported;
    private Boolean maxCompletionTokensSupported;
    private Boolean customTemperatureSupported;
    private List<String> reasoningEfforts;
    private Boolean skillsSupported;
}