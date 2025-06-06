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
public class ConversationMessageDto {

    private String role;
    private String content;
    private List<List<String>> templateMapping;
    private ModelResourceDto model;
    private ConversationMessageSettingsDto settings;
    private String responseId;
}

