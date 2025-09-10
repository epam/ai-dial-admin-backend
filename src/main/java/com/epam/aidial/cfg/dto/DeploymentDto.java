package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class DeploymentDto extends RoleBasedDto {

    @NotBlank(message = "Name is required")
    private String name;

    private String endpoint;

    private String displayName;

    private String displayVersion;

    private String iconUrl;

    private String description;

    private String reference;

    private Set<String> userRoles;

    private Boolean forwardAuthToken;

    private FeaturesDto features = new FeaturesDto();

    private List<String> inputAttachmentTypes;

    private Integer maxInputAttachments;

    private Map<String, Object> defaults;

    private List<String> interceptors;

    private List<String> topics;

    @Min(value = 1, message = "Max retry attempts should be greater than 0")
    private Integer maxRetryAttempts;
}
