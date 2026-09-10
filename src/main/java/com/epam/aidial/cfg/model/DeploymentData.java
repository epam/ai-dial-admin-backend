package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.util.List;
import java.util.Map;

@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class DeploymentData {

    private String id;
    private String model;
    private String application;
    private String toolset;
    private String displayName;
    private String displayVersion;
    private String iconUrl;
    private String description;
    private String reference;
    private String owner;
    private String object;
    private String status;
    private long createdAt;
    private long updatedAt;
    private ScaleSettingsData scaleSettings;
    private FeaturesData features;
    private List<String> inputAttachmentTypes;
    private Integer maxInputAttachments;
    private Map<String, Object> defaults;
    private Map<String, Object> responsesDefaults;
    private List<String> descriptionKeywords;
    private int maxRetryAttempts;
    private List<String> interfaces;
}
