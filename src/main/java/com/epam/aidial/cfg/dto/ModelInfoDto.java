package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.List;

@Data
public class ModelInfoDto {

    private String name;
    private String overrideName;
    private String endpoint;
    private String displayName;
    private String displayVersion;
    private String description;
    private Boolean forwardAuthToken;
    private List<String> inputAttachmentTypes;
    private Integer maxInputAttachments;
    private List<String> topics;
    private String author;
    private Long createdAtMs;
    private Long updatedAtMs;
    private List<String> dependencies;
    private ModelTypeDto type;
    private String tokenizerModel;
    private TokenLimitsDto limits;
    private PricingDto pricing;
    private List<String> fieldsHashingOrder;

}
