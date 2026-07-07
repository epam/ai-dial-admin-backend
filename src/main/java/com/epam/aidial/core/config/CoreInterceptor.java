package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper = true)
public class CoreInterceptor extends Deployment {

    /**
     * Supported LLM API interfaces keyed by interface type. Peer of endpoint.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Map<String, CoreDeploymentInterface> interfaces; // 0.46.0
}
