package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Conversation {

    private String path;
    private String version;
    private String folderId;
    private long updatedAt;
    private String author;
    private String name;
    private ModelResource model;
    private String prompt;
    private Double temperature;
    private List<ConversationMessage> messages;
    private String assistantModelId;
}