package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ResourcePathDto {
    @NotEmpty
    private String path;
}
