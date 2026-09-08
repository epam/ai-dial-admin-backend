package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.validation.annotation.UpstreamEndpoint;
import com.epam.aidial.core.config.databind.JsonToStringDeserializer;
import com.epam.aidial.core.config.databind.StringToJsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

/**
 * Per-interface routing configuration of an upstream: a complete provider endpoint, or overrides for
 * key/extraData, that apply for this interface only.
 */
@Data
public class UpstreamInterfaceDto {

    @UpstreamEndpoint
    private String endpoint;

    private String key;

    @JsonDeserialize(using = JsonToStringDeserializer.class)
    @JsonSerialize(using = StringToJsonSerializer.class)
    private String extraData;

    @JsonDeserialize(using = JsonToStringDeserializer.class)
    @JsonSerialize(using = StringToJsonSerializer.class)
    private String secretExtraData;
}
