package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;
import java.util.Map;

/**
 * Created by Aliaksei Kurnosau on 9/11/24.
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ModelDto extends RoleBasedDto {

    @NotBlank(message = "Name is required")
    private String name;
    private String adapter;
    private String endpoint;
    private String displayName;
    private String displayVersion;
    private String iconUrl;
    private String description;
    private String reference;
    private Boolean forwardAuthToken;
    private FeaturesDto features = new FeaturesDto();
    private List<String> inputAttachmentTypes;
    private Integer maxInputAttachments;
    private Map<String, Object> defaults;
    private List<String> interceptors;
    private List<String> topics;
    private Integer maxRetryAttempts;
    private String author;
    private Long createdAt;
    private Long updatedAt;
    private List<String> dependencies;

    private ModelTypeDto type;
    private String tokenizerModel;
    private TokenLimitsDto limits;
    private PricingDto pricing;
    private List<UpstreamDto> upstreams = List.of();
    private String overrideName;
    private List<String> fieldsHashingOrder;
    private String endpointDeploymentName;

}
