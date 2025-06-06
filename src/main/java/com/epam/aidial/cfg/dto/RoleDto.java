package com.epam.aidial.cfg.dto;


import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class RoleDto {

    @NotBlank(message = "Name is required")
    private String name;
    private String description;
    @Valid
    private Map<@NotBlank(message = "Deployment name is required") String, @Valid LimitDto> limits;

    private List<String> grantedKeys;
}
