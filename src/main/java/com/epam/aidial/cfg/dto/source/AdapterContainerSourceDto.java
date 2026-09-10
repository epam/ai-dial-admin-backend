package com.epam.aidial.cfg.dto.source;

import jakarta.validation.constraints.NotNull;

public record AdapterContainerSourceDto(
        @NotNull String containerId,
        String containerName,
        String completionEndpointPath,
        String responsesEndpointPath
) implements AdapterSourceDto {
}
