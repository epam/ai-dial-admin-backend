package com.epam.aidial.cfg.dto.source;

import jakarta.validation.constraints.NotNull;

public record ToolSetMcpRegistrySourceDto(
        @NotNull String serverName,
        String serverVersion
) implements ToolSetSourceDto {
}
