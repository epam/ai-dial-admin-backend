package com.epam.aidial.cfg.dto.source;

import jakarta.validation.constraints.NotNull;

import java.net.URI;

public record ApplicationSchemaSourceDto(
        @NotNull URI applicationTypeSchemaId
) implements ApplicationSourceDto {
}
