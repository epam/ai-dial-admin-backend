package com.epam.aidial.core.config;

import com.epam.aidial.core.config.databind.CoreUpstreamAuthConfigDeserializer;
import com.epam.aidial.core.config.databind.CoreUpstreamAuthConfigSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * Core's {@code UpstreamAuthConfig}: a plain string ({@code API_KEY}, {@code BEARER}) or an object
 * {@code {type, headerName}}, where {@code headerName} is required for {@code CUSTOM_HEADER} only.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(using = CoreUpstreamAuthConfigDeserializer.class)
@JsonSerialize(using = CoreUpstreamAuthConfigSerializer.class)
public class CoreUpstreamAuthConfig {

    private CoreUpstreamAuthType type;

    private String headerName;

    public static CoreUpstreamAuthConfig of(CoreUpstreamAuthType type) {
        return new CoreUpstreamAuthConfig(type, null);
    }

    public static CoreUpstreamAuthConfig customHeader(String headerName) {
        return new CoreUpstreamAuthConfig(CoreUpstreamAuthType.CUSTOM_HEADER, headerName);
    }
}
