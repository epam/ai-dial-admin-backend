package com.epam.aidial.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

public abstract class CoreUpstreamMixinForCoreObjectMapper {

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String key;

    @JsonSerialize(using = ToStringSerializer.class)
    private String extraData;
}