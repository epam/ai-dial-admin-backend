package com.epam.aidial.cfg.dao.model;

import jakarta.persistence.Embeddable;
import lombok.Data;

@Data
@Embeddable
public class FeaturesEntity {
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
}