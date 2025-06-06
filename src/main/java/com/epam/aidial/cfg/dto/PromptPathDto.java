package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class PromptPathDto {
    @NotEmpty
    private String path;
}
