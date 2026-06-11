package com.epam.aidial.cfg.dto.source;

import jakarta.validation.constraints.NotNull;

public record ApplicationResourceSchemaSourceDto(
        @NotNull String applicationTypeSchemaId
) implements ApplicationResourceSourceDto {
}
