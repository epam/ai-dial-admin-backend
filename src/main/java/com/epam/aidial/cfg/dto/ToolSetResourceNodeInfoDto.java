package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.List;

@Data
public class ToolSetResourceNodeInfoDto {

    private String path;
    private String name;
    private String version;
    private String folderId;
    private Long updateTime;
    private String author;
    private NodeTypeDto nodeType;
    private String nextToken;
    private List<ToolSetResourceNodeInfoDto> items;

}
