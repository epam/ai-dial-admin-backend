package com.epam.aidial.cfg.dto;

import lombok.Data;

@Data
public class ImportPromptResultDto {
    private final String sourcePath;
    private final String targetPath;
    private final ImportPromptStatusDto status;
    private final String error;
}
