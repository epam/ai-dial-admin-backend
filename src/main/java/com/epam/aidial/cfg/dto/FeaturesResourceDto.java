package com.epam.aidial.cfg.dto;

import lombok.Data;

@Data
public class FeaturesResourceDto {
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
    private Boolean addonsSupported;
    private Boolean cacheSupported;
    private Boolean autoCachingSupported;
    private Boolean parallelToolCallsSupported;
    private boolean assistantAttachmentsInRequestSupported;
}
