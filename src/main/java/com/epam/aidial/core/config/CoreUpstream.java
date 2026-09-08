package com.epam.aidial.core.config;

import com.epam.aidial.cfg.utils.SecretUtils;
import com.epam.aidial.core.config.databind.JsonToStringDeserializer;
import com.epam.aidial.core.config.databind.StringToJsonSerializer;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoreUpstream {

    @JsonAlias({"endpoint", "dial:endpoint"})
    private String endpoint;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonAlias({"responsesEndpoint", "responses_endpoint", "dial:responsesEndpoint"})
    private String responsesEndpoint;  // 0.42.0
    @JsonAlias({"key", "dial:key"})
    private String key;

    @JsonDeserialize(using = JsonToStringDeserializer.class)
    @JsonSerialize(using = StringToJsonSerializer.class)
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonAlias({"extraData", "dial:extraData"})
    private String extraData;

    @JsonAlias({"weight", "dial:weight"})
    private int weight = 1;

    @JsonAlias({"tier", "dial:tier"})
    private int tier = 0;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonAlias({"id", "dial:id"})
    private String id;   // 0.44.0

    @JsonDeserialize(using = JsonToStringDeserializer.class)
    @JsonSerialize(using = StringToJsonSerializer.class)
    @JsonAlias({"secretExtraData", "dial:secretExtraData"})
    private String secretExtraData;   // 0.45.0

    /**
     * Root url that every {@link #interfaces} entry declaring no endpoint of its own resolves against
     * (Core appends its own fixed API path for the interface type). Unlike a deployment's
     * {@code base_url}, the ingress path plays no part — an upstream is addressed where its own API
     * spec says it lives.
     */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @JsonAlias({"baseUrl", "base_url", "dial:baseUrl"})
    private String baseUrl; // 0.48.0

    /**
     * Provider urls keyed by interface-type value. Peer of endpoint/responsesEndpoint.
     */
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonAlias({"interfaces", "dial:interfaces"})
    private Map<String, CoreUpstreamInterface> interfaces = Map.of(); // 0.48.0

    public String toString() {
        return "Upstream(endpoint=" + this.getEndpoint() + ", responsesEndpoint=" + this.responsesEndpoint
                + ", key=" + SecretUtils.mask(this.getKey())
                + ", extraData=" + this.getExtraData()
                + ", secretExtraData=" + SecretUtils.mask(this.getSecretExtraData())
                + ", weight=" + this.getWeight()
                + ", tier=" + this.getTier()
                + ", baseUrl=" + this.getBaseUrl() + ")";
    }
}