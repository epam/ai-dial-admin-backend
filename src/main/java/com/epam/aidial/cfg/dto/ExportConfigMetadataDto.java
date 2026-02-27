package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.List;

@Data
public class ExportConfigMetadataDto {

    private List<ExportConfigComponentMetadataDto> components;
}
