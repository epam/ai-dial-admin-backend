package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ConversationMessageSettingsDto {

    private String prompt;
    private BigDecimal temperature;
}

