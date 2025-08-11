package com.epam.aidial.cfg.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.net.URI;
import java.util.List;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Application extends RoleBased {

    private String endpoint;
    private String iconUrl;
    private String reference;
    private String description;
    private String displayName;
    private String displayVersion;
    private List<String> inputAttachmentTypes;
    private Integer maxInputAttachments;
    private Boolean forwardAuthToken;
    private List<String> descriptionKeywords;
    private Integer maxRetryAttempts;
    private Map<String, Object> defaults;
    private List<String> interceptors;
    private String author;
    private Long createdAt;
    private Long updatedAt;
    private List<String> dependencies;
    private Features features;
    private Map<String, Object> applicationProperties;
    private URI applicationTypeSchemaId;
    private String viewerUrl;
    private String editorUrl;

}
