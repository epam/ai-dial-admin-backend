package com.epam.aidial.cfg.domain.model;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class ApplicationTypeSchema {

    private String schemaId;
    private String schema;
    private String description;
    private String applicationTypeEditorUrl;
    private String applicationTypeViewerUrl;
    private String applicationTypeDisplayName;
    private String applicationTypeCompletionEndpoint;
    private Map<String, String> defs;
    private Map<String, String> properties;
    private List<String> required;
    private List<String> applications;
    private Set<String> topics;
}
