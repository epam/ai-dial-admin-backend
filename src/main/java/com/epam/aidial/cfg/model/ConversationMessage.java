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
public class ConversationMessage {

    private String role;
    private String content;
    private List<List<String>> templateMapping;
    private ModelResource model;
    private ConversationMessageSettings settings;
    private String responseId;
}

