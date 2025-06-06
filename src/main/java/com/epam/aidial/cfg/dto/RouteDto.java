package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.validation.annotation.HttpMethod;
import com.epam.aidial.cfg.dto.validation.annotation.RoutePath;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Set;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class RouteDto extends RoleBasedDto {

    @NotBlank(message = "Name is required")
    private String name;
    private String description;
    @Valid
    private ResponseDto response;
    private boolean rewritePath;
    @NotEmpty
    private List<@RoutePath String> paths;
    private Set<@HttpMethod String> methods;
    @Valid
    private List<UpstreamDto> upstreams;
    private int maxRetryAttempts;
}
