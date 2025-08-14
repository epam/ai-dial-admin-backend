package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.dto.validation.annotation.HttpMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.Set;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class Route extends RoleBased {

    private String description;
    @Valid
    private Response response;
    private boolean rewritePath;
    @NotEmpty
    private List<@NotNull String> paths;
    private Set<@HttpMethod String> methods;
    @Valid
    private List<Upstream> upstreams;
    private int maxRetryAttempts;
    private Long createdAt;
    private Long updatedAt;
}
