package com.epam.aidial.cfg.domain.model;

import lombok.Data;

/**
 * External service an application authenticates against. Authentication is configured via the
 * resource auth settings (OAuth, API key, or none), the same mechanism used by toolsets.
 */
@Data
public class ExternalService {

    private String displayName;
    private String description;
    private ResourceAuthSettings authSettings;
}
