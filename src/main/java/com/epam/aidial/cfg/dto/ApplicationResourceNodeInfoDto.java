package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.List;

@Data
public class ApplicationResourceNodeInfoDto {

    private String path;
    private String name;
    private String version;
    private String folderId;
    private Long updatedAt;
    private String author;
    private NodeTypeDto nodeType;
    private String nextToken;
    private List<ApplicationResourceNodeInfoDto> items;

}