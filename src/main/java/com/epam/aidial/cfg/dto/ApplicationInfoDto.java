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

}
