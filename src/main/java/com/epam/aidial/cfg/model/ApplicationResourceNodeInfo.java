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
public class ApplicationResourceNodeInfo {

    private String path;
    private String name;
    private String version;
    private String folderId;
    private Long updatedAt;
    private String author;
    private NodeType nodeType;
    private List<ApplicationResourceNodeInfo> items;
    private String nextToken;

}