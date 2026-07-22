package com.epam.aidial.cfg.dto;

import lombok.Data;

@Data
public class ExternalServiceResourceDto {

    private String displayName;
    private String description;
    private CoreResourceAuthSettingsDto authSettings;
}
