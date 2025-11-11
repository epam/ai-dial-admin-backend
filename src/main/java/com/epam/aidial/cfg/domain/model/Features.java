package com.epam.aidial.cfg.domain.model;

import lombok.Data;

/**
 * Maintain the same default values in:
 * <ul>
 *   <li>
 *       {@link com.epam.aidial.cfg.dao.model.FeaturesEntity}, since it's directly created
 *       here: {@link com.epam.aidial.cfg.domain.service.AssistantsPropertyService#createEntityInstance()}
 *   </li>
 *   <li>
 *       {@link com.epam.aidial.cfg.domain.model.Features}, since the object of that class can be created when mapping
 *       from {@link com.epam.aidial.core.config.CoreFeatures} or when deserializing Admin config during import
 *   </li>
 *   <li>
 *      {@link com.epam.aidial.cfg.dto.FeaturesDto}, since it's created during deserialization of web request body
 *   </li>
 * </ul>
 */

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
    private boolean allowResume = true;
    private boolean accessibleByPerRequestKey = true;
    private boolean contentPartsSupported;
    private boolean temperatureSupported = true;
    private Boolean cacheSupported;
    private Boolean autoCachingSupported;
    private Boolean consentRequired;
    private boolean parallelToolCallsSupported = true;
    private boolean assistantAttachmentsInRequestSupported;
}
