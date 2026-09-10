package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FeaturesData {

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