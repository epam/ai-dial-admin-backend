package com.epam.aidial.cfg.dto;


import lombok.Data;

import java.util.List;

@Data
public class ImportResourcesFileResultDto {

    private String error;
    private List<ImportPromptResultDto> importResults;

}
