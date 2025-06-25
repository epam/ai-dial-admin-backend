package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.List;

@Data
public class ConversationMessageDto {

    private String role;
    private String content;
    private List<List<String>> templateMapping;
    private ModelResourceDto model;
    private ConversationMessageSettingsDto settings;
    private String responseId;
}

