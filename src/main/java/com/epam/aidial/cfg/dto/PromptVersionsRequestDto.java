package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class PromptVersionsRequestDto {
    @NotEmpty
    private String folderId;
    @NotEmpty
    private String name;
}
