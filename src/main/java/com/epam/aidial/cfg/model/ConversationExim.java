package com.epam.aidial.cfg.model;

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
public class ConversationExim {

    private String id;
    private String version;
    private String folderId;
    private String name;
    private ModelResource model;
    private String prompt;
    private Double temperature;
    private List<ConversationMessage> messages;
    private String assistantModelId;
    private Map<String, Object> customViewState;
}