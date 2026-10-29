package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.domain.model.databind.UpstreamAuthConfigDeserializer;
import com.epam.aidial.cfg.domain.model.databind.UpstreamAuthConfigSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

/**
 * How the key of an upstream is carried to it. Either a plain type ({@code API_KEY}, {@code BEARER}),
 * or {@code CUSTOM_HEADER} together with the {@link #headerName} the key is sent in.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonDeserialize(using = UpstreamAuthConfigDeserializer.class)
@JsonSerialize(using = UpstreamAuthConfigSerializer.class)
public class UpstreamAuthConfig {

    private UpstreamAuthType type;

    // only for CUSTOM_HEADER
    private String headerName;

    public static UpstreamAuthConfig of(UpstreamAuthType type) {
        return new UpstreamAuthConfig(type, null);
    }

    public static UpstreamAuthConfig customHeader(String headerName) {
        return new UpstreamAuthConfig(UpstreamAuthType.CUSTOM_HEADER, headerName);
    }
}
