package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.domain.value.LocalizedValue;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DeploymentDataDto {

    private String id;
    private String model;
    private String application;
    private String toolset;
    private LocalizedValue displayName;
    private String displayVersion;
    private String iconUrl;
    private LocalizedValue description;
    private LocalizedValue intro;
    private String reference;
    private String owner;
    private String object;
    private String status;
    private Long createdAt;
    private Long updatedAt;
    private ScaleSettingsDataDto scaleSettings;
    private FeaturesDataDto features;
    private List<String> inputAttachmentTypes;
    private Integer maxInputAttachments;
    private Map<String, Object> defaults;
    private Map<String, Object> responsesDefaults;
    private List<String> descriptionKeywords;
    private Integer maxRetryAttempts;
    private List<String> interfaces;
}