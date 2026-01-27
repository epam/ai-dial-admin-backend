package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

import java.util.List;

/**
 * The class describes metadata of the MCP server.
 */
@Data
@Accessors(chain = true)
@EqualsAndHashCode(callSuper = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CoreToolSet extends CoreSecuredResource {

    private Transport transport;

    @JsonAlias({"allowedTools", "allowed_tools"})
    private List<String> allowedTools = List.of();

    public enum Transport {
        HTTP, SSE
    }

    public CoreToolSet() {
        super();
    }

    @JsonIgnore
    public static CoreToolSet empty() {
        CoreToolSet coreToolSet = new CoreToolSet();

        coreToolSet.setAllowedTools(null);
        coreToolSet.setForwardPerRequestKey(null);
        coreToolSet.setAuthSettings(null);
        coreToolSet.setForwardAuthToken(null);
        coreToolSet.setDefaults(null);
        coreToolSet.setInterceptors(null);
        coreToolSet.setDescriptionKeywords(null);
        coreToolSet.setMaxRetryAttempts(null);
        coreToolSet.setDependencies(null);

        return coreToolSet;
    }
}
