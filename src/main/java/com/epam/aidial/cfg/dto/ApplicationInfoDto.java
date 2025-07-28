package com.epam.aidial.cfg.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.List;

@Data
@EqualsAndHashCode(exclude = {"createdAt", "updatedAt"})
public class ApplicationInfoDto {

    private String name;
    private String endpoint;
    private String displayName;
    private String displayVersion;
    private String description;
    private Boolean forwardAuthToken;
    private List<String> inputAttachmentTypes;
    private Integer maxInputAttachments;
    private List<String> topics;
    private String author;
    private Instant createdAt;
    private Instant updatedAt;
    private List<String> dependencies;

    private String viewerUrl;
    private String editorUrl;

}
