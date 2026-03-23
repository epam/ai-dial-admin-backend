package com.epam.aidial.cfg.domain.model.route;

import com.epam.aidial.cfg.domain.model.Response;
import com.epam.aidial.cfg.domain.model.RoleBased;
import com.epam.aidial.cfg.domain.model.Upstream;
import com.epam.aidial.cfg.dto.validation.annotation.HttpMethod;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;
import java.util.SortedSet;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public abstract class BaseRoute extends RoleBased {

    private String description;
    @Valid
    private Response response;
    private boolean rewritePath;
    @NotEmpty
    private List<@NotNull String> paths;
    private SortedSet<@HttpMethod String> methods;
    @Valid
    private List<Upstream> upstreams;
    private Integer maxRetryAttempts;
    private Long createdAt;
    private Long updatedAt;
    private int order = Integer.MAX_VALUE;
}
