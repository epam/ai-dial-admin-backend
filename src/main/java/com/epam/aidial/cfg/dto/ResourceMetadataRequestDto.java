package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.validation.annotation.MetadataPath;
import lombok.Data;

import javax.annotation.Nullable;

@Data
public class ResourceMetadataRequestDto {
    private boolean recursive;
    @MetadataPath
    private String path;
    @Nullable
    private String nextToken;
    private Integer limit;
}
