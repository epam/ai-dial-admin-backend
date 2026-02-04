package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.List;

@Data
public class FolderInfoDto {
    private String name;
    private String parentPath;
    private String bucket;
    private String path;
    private List<ResourceAccessTypeDto> permissions;
    private List<FolderInfoDto> items;
}
