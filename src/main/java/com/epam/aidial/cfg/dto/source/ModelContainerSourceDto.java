package com.epam.aidial.cfg.dto.source;

import jakarta.validation.constraints.NotNull;

public record ModelContainerSourceDto(
        @NotNull String containerId,
        String completionEndpointPath
) implements ModelSourceDto {
}
