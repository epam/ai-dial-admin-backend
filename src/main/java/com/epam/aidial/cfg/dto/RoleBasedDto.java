package com.epam.aidial.cfg.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class RoleBasedDto {

    private Map<@NotBlank(message = "Role name is required") String, @Valid LimitDto> roleLimits;
    private Map<@NotBlank(message = "Role name is required") String, @Valid ShareResourceLimitDto> roleShareResourceLimits;
    private Boolean isPublic = false;
    private LimitDto defaultRoleLimit;
    private ShareResourceLimitDto defaultRoleShareResourceLimit;
}
