package com.epam.aidial.cfg.dto;


import com.epam.aidial.cfg.dto.validation.annotation.RoleLimits;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Map;

@Data
public class RoleBasedDto {
    @RoleLimits
    private Map<@NotBlank(message = "Role name is required") String, @Valid LimitDto> roleLimits;
    private Boolean isPublic = false;
    private LimitDto defaultRoleLimit;
}
