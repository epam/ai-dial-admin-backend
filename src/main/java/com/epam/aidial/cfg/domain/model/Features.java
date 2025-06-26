package com.epam.aidial.cfg.domain.model;

import lombok.Data;

@Data
public class Features {
    private String rateEndpoint;
    private String tokenizeEndpoint;
    private String truncatePromptEndpoint;
    private String configurationEndpoint;

    private boolean systemPromptSupported = true;
    private boolean toolsSupported;
    private boolean seedSupported;

    private boolean urlAttachmentsSupported;
    private boolean folderAttachmentsSupported;
    private boolean allowResume;
    private boolean accessibleByPerRequestKey = true;
    private boolean contentPartsSupported;
    private boolean temperatureSupported = true;
    private boolean addonsSupported;

    private Boolean cacheSupported;

    private Boolean autoCachingSupported;
    private Boolean consentRequired;

    private Boolean parallelToolCallsSupported = true;
}