package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Prompt {

    private String path;
    private String name;
    private String version;
    private String folderId;
    private String description;
    private long updateTime;
    private String author;
    private String content;

}
