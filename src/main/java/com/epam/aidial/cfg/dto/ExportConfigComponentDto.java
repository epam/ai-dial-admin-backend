package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
public class ExportConfigComponentDto {

    @NotBlank
    private String name;
    @NotNull
    private ExportConfigComponentTypeDto type;
    private Set<ExportConfigComponentTypeDto> dependencies = new HashSet<>();

}
