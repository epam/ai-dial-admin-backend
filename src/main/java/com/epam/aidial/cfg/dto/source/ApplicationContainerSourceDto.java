package com.epam.aidial.cfg.dto.source;

import jakarta.validation.constraints.NotNull;

public record ApplicationContainerSourceDto(
        @NotNull String containerId,
        String containerName,
        String completionEndpointPath,
        String mcpEndpointPath
) implements ApplicationSourceDto {
}
