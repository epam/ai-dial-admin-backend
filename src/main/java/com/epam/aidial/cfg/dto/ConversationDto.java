package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConversationDto {

    private String path;
    private String version;
    private String folderId;
    private long updatedAt;
    private String author;
    private String name;
    private ModelResourceDto model;
    private String prompt;
    private Double temperature;
    private List<ConversationMessageDto> messages;
    private String assistantModelId;
}