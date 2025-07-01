package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.List;

@Data
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
    private Long createdAt;
    private Long updatedAt;
    private List<String> dependencies;

    private String viewerUrl;
    private String editorUrl;

}
