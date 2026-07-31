package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.domain.value.LocalizedValue;
import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class ModelInfoDto {

    private String name;
    private String overrideName;
    private String endpoint;
    private LocalizedValue displayName;
    private LocalizedValue displayVersion;
    private String description;
    private Boolean forwardAuthToken;
    private List<String> inputAttachmentTypes;
    private Integer maxInputAttachments;
    private List<String> topics;
    private String author;
    private Instant createdAt;
    private Instant updatedAt;
    private List<String> dependencies;
    private ModelTypeDto type;
    private String tokenizerModel;
    private TokenLimitsDto limits;
    private PricingDto pricing;
    private List<String> fieldsHashingOrder;

}
