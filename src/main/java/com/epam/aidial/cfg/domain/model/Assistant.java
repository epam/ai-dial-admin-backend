package com.epam.aidial.cfg.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Assistant extends RoleBased {

    private String iconUrl;
    private String description;
    private String displayName;
    private List<String> inputAttachmentTypes;
    private Integer maxInputAttachments;
    private Boolean forwardAuthToken;
    private List<String> topics;
    private Map<String, Object> defaults;
    private String author;
    private Long createdAt;
    private Long updatedAt;
    private List<String> dependencies;
}
