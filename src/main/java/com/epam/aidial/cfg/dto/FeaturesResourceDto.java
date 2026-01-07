package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.validation.annotation.Endpoint;
import lombok.Data;

@Data
public class FeaturesResourceDto {
    @Endpoint
    private String rateEndpoint;
    @Endpoint
    private String tokenizeEndpoint;
    @Endpoint
    private String truncatePromptEndpoint;
    @Endpoint
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
    private boolean assistantAttachmentsInRequestSupported;
    private Boolean supportCommentInRateResponse;
}