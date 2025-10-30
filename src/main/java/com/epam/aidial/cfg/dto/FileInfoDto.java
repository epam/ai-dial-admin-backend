package com.epam.aidial.cfg.dto;


import lombok.Data;

@Data
public class FileInfoDto {

    private String path;
    private String name;
    private String folderId;
    private long updatedAt;
    private String author;
    private long contentLength;
    private String contentType;

}