package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.validation.annotation.HttpMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.Instant;
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
    private List<@NotNull String> paths;
    private Set<@HttpMethod String> methods;
    @Valid
    private List<UpstreamDto> upstreams;
    private int maxRetryAttempts;
    private Instant createdAt;
    private Instant updatedAt;
}
