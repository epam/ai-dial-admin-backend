package com.epam.aidial.cfg.dto.route;

import com.epam.aidial.cfg.dto.ResponseDto;
import com.epam.aidial.cfg.dto.RoleBasedDto;
import com.epam.aidial.cfg.dto.UpstreamDto;
import com.epam.aidial.cfg.dto.validation.annotation.HttpMethod;
import com.epam.aidial.cfg.dto.validation.annotation.RoutePath;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.Instant;
import java.util.List;
import java.util.SortedSet;

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
    private List<@RoutePath String> paths;
    private SortedSet<@HttpMethod String> methods;
    @Valid
    private List<UpstreamDto> upstreams;
    @Positive(message = "Max retry attempts should be greater than 0")
    private Integer maxRetryAttempts;
    @EqualsAndHashCode.Exclude
    private Instant createdAt;
    @EqualsAndHashCode.Exclude
    private Instant updatedAt;
    @Min(value = 0, message = "Order can't be negative")
    private Integer order;
}
