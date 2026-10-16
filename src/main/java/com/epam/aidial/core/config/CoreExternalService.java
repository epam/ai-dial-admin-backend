package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.experimental.Accessors;
import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * External service an application authenticates against. Authentication is configured via the
 * resource auth settings (OAuth, API key, or none), the same mechanism used by toolsets.
 */
@Data
@Accessors(chain = true)
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CoreExternalService { // 0.46.0

    @JsonAlias({"displayName", "display_name"})
    private String displayName;

    private String description;

    @JsonAlias({"authSettings", "auth_settings"})
    private CoreResourceAuthSettings authSettings;
}
