package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import tools.jackson.databind.annotation.JsonSerialize;
import tools.jackson.databind.ser.std.ToStringSerializer;

public abstract class CoreUpstreamInterfaceMixinForCoreObjectMapper {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String key;

    @JsonSerialize(using = ToStringSerializer.class)
    private String extraData;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @JsonSerialize(using = ToStringSerializer.class)
    private String secretExtraData;
}
