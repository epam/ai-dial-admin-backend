package com.epam.aidial.cfg.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
public class SelectedItemsExportRequestDto extends ExportRequestDto {

    @NotEmpty
    private List<ExportConfigComponentDto> components;
}
