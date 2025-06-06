package com.epam.aidial.cfg.dto;


import lombok.Data;

import java.util.List;

@Data
public class FileNodeInfoDto {

    private String path;
    private String name;
    private String folderId;
    private long updateTime;
    private String author;
    private long contentLength;
    private String contentType;
    private NodeTypeDto nodeType;
    private String nextToken;
    private List<FileNodeInfoDto> items;

}
