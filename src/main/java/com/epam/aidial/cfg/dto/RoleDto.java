package com.epam.aidial.cfg.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
public class RoleDto {

    @NotBlank(message = "Name is required")
    private String name;
    private String description;
    @NotBlank(message = "DisplayName is required")
    private String displayName;
    @EqualsAndHashCode.Exclude
    private Instant createdAt;
    @EqualsAndHashCode.Exclude
    private Instant updatedAt;
    @Valid
    private Map<@NotBlank(message = "Deployment name is required") String, @Valid LimitDto> limits;
    @Valid
    private Map<ResourceTypeDto, @Valid ShareResourceLimitDto> share;

    private List<String> grantedKeys;
    private CostLimitDto costLimit = new CostLimitDto();

}
