package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Set;

@EqualsAndHashCode(callSuper = true)
@Data
public class FullExportRequestDto extends ExportRequestDto {

    @NotEmpty
    private Set<ExportConfigComponentType> componentTypes;
}
