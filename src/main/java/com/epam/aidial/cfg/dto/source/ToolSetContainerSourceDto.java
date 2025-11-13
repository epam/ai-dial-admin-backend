package com.epam.aidial.cfg.dto.source;

import jakarta.validation.constraints.NotNull;

public record ToolSetContainerSourceDto(
        @NotNull String containerId,
        String containerName,
        String completionEndpointPath
) implements ToolSetSourceDto {
}
