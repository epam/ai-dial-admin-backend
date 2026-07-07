package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.experimental.Accessors;

/**
 * Per-interface routing configuration of a deployment. The interface type is the key
 * in the {@code interfaces} map; the value declares the source (adapter) root the matching
 * ingress path is appended to at request time.
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CoreDeploymentInterface { // 0.46.0

    @NotNull(message = "base_url must be defined")
    @JsonAlias({"baseUrl", "base_url"})
    private String baseUrl;
}
