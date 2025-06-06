package com.epam.aidial.cfg.model;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileNodeInfo {

    private String path;
    private String name;
    private String folderId;
    private Long updateTime;
    private String author;
    private long contentLength;
    private String contentType;
    private NodeType nodeType;
    private List<FileNodeInfo> items;
    private String nextToken;

}
