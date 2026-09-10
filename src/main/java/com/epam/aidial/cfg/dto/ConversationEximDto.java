package com.epam.aidial.cfg.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationEximDto {

    private String id;
    private String version;
    private String folderId;
    private String name;
    private ModelResourceDto model;
    private String prompt;
    private Double temperature;
    private List<ConversationMessageDto> messages;
    private String assistantModelId;
    private Map<String, Object> customViewState;
}