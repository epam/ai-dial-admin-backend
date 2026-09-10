package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.utils.SecretUtils;
import com.epam.aidial.core.config.databind.JsonToStringDeserializer;
import com.epam.aidial.core.config.databind.StringToJsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

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

    private int weight;

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