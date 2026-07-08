package com.epam.aidial.cfg.dto;

import jakarta.validation.Valid;
import lombok.Data;

/**
 * External service an application authenticates against. Authentication is configured via the
 * resource auth settings (OAuth, API key, or none), the same mechanism used by toolsets.
 */
@Data
public class ExternalServiceDto {

    private String displayName;
    private String description;
    @Valid
    private ResourceAuthSettingsDto authSettings;
}
