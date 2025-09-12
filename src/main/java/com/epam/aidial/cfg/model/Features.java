package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Features {
    private String rateEndpoint;
    private String tokenizeEndpoint;
    private String truncatePromptEndpoint;
    private String configurationEndpoint;
    private boolean systemPromptSupported;
    private boolean toolsSupported;
    private boolean seedSupported;
    private boolean urlAttachmentsSupported;
    private boolean folderAttachmentsSupported;
    private boolean allowResume;
    private boolean accessibleByPerRequestKey;
    private boolean contentPartsSupported;
    private boolean consentRequired;
    private boolean temperatureSupported;
    private boolean addonsSupported;
    private boolean cacheSupported;
    private boolean autoCachingSupported;
    private boolean parallelToolCallsSupported;
}
