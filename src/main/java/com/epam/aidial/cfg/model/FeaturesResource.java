package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FeaturesResource {
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
}