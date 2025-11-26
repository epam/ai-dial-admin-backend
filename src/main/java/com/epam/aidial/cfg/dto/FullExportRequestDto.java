package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class FullExportRequestDto extends ExportRequestDto {

    @NotEmpty
    private Set<ExportConfigComponentTypeDto> componentTypes;
    private Set<String> topics;
}
