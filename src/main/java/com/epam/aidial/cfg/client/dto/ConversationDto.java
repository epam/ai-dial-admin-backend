package com.epam.aidial.cfg.client.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConversationDto {

    private String id;
    private String name;
    private ModelResourceDto model;
    private String prompt;
    private Double temperature;
    private String folderId;
    private List<ConversationMessageDto> messages;
    private String assistantModelId;
    private Long updatedAt;
}

