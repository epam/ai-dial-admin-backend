package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.Set;

@Data
public class ExportConfigComponentMetadataDto {

    private ExportConfigComponentTypeDto type;
    private Set<ExportConfigComponentTypeDto> dependencies;
}
