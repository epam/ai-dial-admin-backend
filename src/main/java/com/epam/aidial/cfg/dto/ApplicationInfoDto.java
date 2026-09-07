package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.source.ApplicationSourceDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
public class ApplicationInfoDto {

    private String name;
    private String endpoint;
    private String displayName;
    private String displayVersion;
    private String overrideName;
    private Map<String, String> defaultHeaders;
    private String iconUrl;
    private String description;
    private Boolean forwardAuthToken;
    private List<String> inputAttachmentTypes;
    private Integer maxInputAttachments;
    private List<String> topics;
    private String author;
    @EqualsAndHashCode.Exclude
    private Instant createdAt;
    @EqualsAndHashCode.Exclude
    private Instant updatedAt;
    private List<String> dependencies;

    private String viewerUrl;
    private String editorUrl;
    private ApplicationSourceDto source;
    private ValidityStateDto validityState;

}