package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class ConversationDto {

    private String id;
    private String path;
    private String version;
    private String folderId;
    private long updatedAt;
    private Long lastActivityDate;
    private String author;
    private String name;
    private ModelResourceDto model;
    private String prompt;
    private Double temperature;
    private List<ConversationMessageDto> messages;
    private String assistantModelId;
    private Map<String, Object> customViewState;
}