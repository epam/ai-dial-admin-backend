package com.epam.aidial.core.config;

import com.epam.aidial.cfg.utils.SecretUtils;
import com.epam.aidial.core.config.databind.JsonToStringDeserializer;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Per-interface configuration for a {@link CoreUpstream}. Every field overrides its
 * {@link CoreUpstream}-level namesake for this interface only; a field left unset here falls back to
 * the upstream's own value.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CoreUpstreamInterface { // 0.48.0

    @JsonAlias({"endpoint", "dial:endpoint"})
    private String endpoint;

    @JsonAlias({"key", "dial:key"})
    private String key;

    @JsonDeserialize(using = JsonToStringDeserializer.class)
    @JsonAlias({"extraData", "dial:extraData"})
    private String extraData;

    @JsonDeserialize(using = JsonToStringDeserializer.class)
    @JsonAlias({"secretExtraData", "dial:secretExtraData"})
    private String secretExtraData;

    public String toString() {
        return "UpstreamInterface(endpoint=" + this.getEndpoint() + ", key=" + SecretUtils.mask(this.getKey())
                + ", extraData=" + this.getExtraData()
                + ", secretExtraData=" + SecretUtils.mask(this.getSecretExtraData()) + ")";
    }
}
