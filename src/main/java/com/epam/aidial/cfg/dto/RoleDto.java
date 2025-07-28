package com.epam.aidial.cfg.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
public class RoleDto {

    @NotBlank(message = "Name is required")
    private String name;
    private String description;
    private Instant createdAt;
    private Instant updatedAt;
    @Valid
    private Map<@NotBlank(message = "Deployment name is required") String, @Valid LimitDto> limits;
    @Valid
    private Map<@NotBlank(message = "Deployment name is required") String, @Valid ShareResourceLimitDto> share;

    private List<String> grantedKeys;

}
