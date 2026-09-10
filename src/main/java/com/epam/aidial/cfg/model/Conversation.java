package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Conversation {

    private String id;
    private String path;
    private String version;
    private String folderId;
    private long updatedAt;
    private Long lastActivityDate;
    private String author;
    private String name;
    private ModelResource model;
    private String prompt;
    private Double temperature;
    private List<ConversationMessage> messages;
    private String assistantModelId;
    private Map<String, Object> customViewState;
}