package com.epam.aidial.cfg.domain.model;

import lombok.Data;

/**
 * External service an application authenticates against; credentials are managed per user
 * via the resource auth settings, same as toolsets.
 */
@Data
public class ExternalService {

    private String displayName;
    private String description;
    private ResourceAuthSettings authSettings;
}
