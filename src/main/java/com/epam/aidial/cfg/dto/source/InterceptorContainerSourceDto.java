package com.epam.aidial.cfg.dto.source;

import jakarta.validation.constraints.NotNull;

public record InterceptorContainerSourceDto(
        @NotNull String containerId,
        String containerName,
        String completionEndpointPath,
        String configurationEndpointPath
) implements InterceptorSourceDto {
}
