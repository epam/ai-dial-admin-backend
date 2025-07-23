package com.epam.aidial.cfg.dto.source;

import jakarta.validation.constraints.NotNull;

public record InterceptorRunnerSourceDto(
        @NotNull String templateName
) implements InterceptorSourceDto {
}
