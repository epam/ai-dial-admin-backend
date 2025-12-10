package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.validation.annotation.MetadataPath;
import lombok.Data;

@Data
public class FilePathDto {
    @MetadataPath
    private String path;
}