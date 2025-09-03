package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.validation.annotation.MetadataPath;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class MoveFolderRequestDto {

    @MetadataPath
    private String oldPath;
    @MetadataPath
    private String newPath;
    private List<@NotNull ResourceTypeDto> resourceTypes;
}
