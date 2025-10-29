package com.epam.aidial.cfg.dto.source;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AdapterSourceDto(
        @NotNull String adapterName,
        @Pattern(
                regexp = "^(|.+?/chat/completions)?$",
                message = "Must end '/chat/completions'"
        )
        String completionEndpointPath
) implements ModelSourceDto {
}