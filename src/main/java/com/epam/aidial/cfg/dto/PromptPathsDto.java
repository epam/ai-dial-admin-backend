package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class PromptPathsDto {
    @NotEmpty
    private List<PromptPathDto> paths;
}
