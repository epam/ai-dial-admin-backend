package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.core.config.databind.JsonToStringDeserializer;
import com.epam.aidial.core.config.databind.StringToJsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

/**
 * Per-interface routing configuration of an {@link Upstream}. Unlike {@link DeploymentInterface}, which
 * carries a base url the matching ingress path is appended to, this carries a complete endpoint of its
 * own: an upstream is the provider itself, not an adapter routed into.
 *
 * <p>Every field overrides its {@link Upstream}-level namesake for this interface only; a field left
 * unset here falls back to the upstream's value.
 */
@Data
public class UpstreamInterface {

    private String endpoint;

    private String key;

    @JsonDeserialize(using = JsonToStringDeserializer.class)
    @JsonSerialize(using = StringToJsonSerializer.class)
    private String extraData;

    @JsonDeserialize(using = JsonToStringDeserializer.class)
    @JsonSerialize(using = StringToJsonSerializer.class)
    private String secretExtraData;
}
