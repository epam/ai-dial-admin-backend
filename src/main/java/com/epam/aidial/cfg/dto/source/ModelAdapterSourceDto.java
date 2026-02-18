package com.epam.aidial.cfg.dto.source;

import jakarta.validation.constraints.NotNull;

public record ModelAdapterSourceDto(
        @NotNull String adapterName,
        String completionEndpointPath
) implements ModelSourceDto {
}
