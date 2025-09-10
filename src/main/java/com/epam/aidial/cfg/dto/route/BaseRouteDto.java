package com.epam.aidial.cfg.dto.route;

import com.epam.aidial.cfg.dto.ResponseDto;
import com.epam.aidial.cfg.dto.RoleBasedDto;
import com.epam.aidial.cfg.dto.UpstreamDto;
import com.epam.aidial.cfg.dto.validation.annotation.HttpMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
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
public abstract class BaseRouteDto extends RoleBasedDto {

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
    @Min(value = 1, message = "Max retry attempts should be greater than 0")
    private int maxRetryAttempts;
    @EqualsAndHashCode.Exclude
    private Instant createdAt;
    @EqualsAndHashCode.Exclude
    private Instant updatedAt;
    @Min(value = 0, message = "Order can't be negative")
    private Integer order;
}
