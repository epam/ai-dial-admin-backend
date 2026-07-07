package com.epam.aidial.cfg.dto;

import jakarta.validation.Valid;
import lombok.Data;

/**
 * External service an application authenticates against; credentials are managed per user
 * via the resource auth settings, same as toolsets.
 */
@Data
public class ExternalServiceDto {

    private String displayName;
    private String description;
    @Valid
    private ResourceAuthSettingsDto authSettings;
}
