package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.experimental.Accessors;

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
