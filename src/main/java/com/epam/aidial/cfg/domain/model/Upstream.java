package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.utils.SecretUtils;
import com.epam.aidial.core.config.databind.JsonToStringDeserializer;
import com.epam.aidial.core.config.databind.StringToJsonSerializer;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import lombok.Data;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.Map;

@Data
public class Upstream {

    private String id;

    private String endpoint;

    private String responsesEndpoint;

    private String key;

    @JsonDeserialize(using = JsonToStringDeserializer.class)
    @JsonSerialize(using = StringToJsonSerializer.class)
    private String extraData;

    // Same default as UpstreamDto/CoreUpstream: MapStruct copies this value over CoreUpstream's own default.
    // Nulls.SKIP keeps the default for an explicit "weight": null instead of the primitive's 0.
    @JsonSetter(nulls = Nulls.SKIP)
    private int weight = 1;

    private int tier;

    @JsonDeserialize(using = JsonToStringDeserializer.class)
    @JsonSerialize(using = StringToJsonSerializer.class)
    private String secretExtraData;

    /**
     * Root url that every {@link #interfaces} entry declaring no endpoint of its own resolves against.
     */
    private String baseUrl;

    /**
     * Provider urls keyed by interface-type value. Peer of endpoint/responsesEndpoint.
     */
    private Map<String, UpstreamInterface> interfaces = Map.of();

    /**
     * HTTP header/scheme used to carry {@link #key} to this upstream on the route path. API_KEY (default)
     * sends it as {@code API-KEY: <key>}; BEARER sends it as {@code Authorization: Bearer <key>}.
     */
    @JsonAlias({"authType", "auth_type"})
    private UpstreamAuthType authType = UpstreamAuthType.API_KEY;

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