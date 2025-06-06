package com.epam.aidial.cfg.dto;

import lombok.Data;

@Data
public class PromptDto {

    private String path;
    private String name;
    private String version;
    private String folderId;
    private String description;
    private long updateTime;
    private String author;
    private String content;

}
