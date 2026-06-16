package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.utils.SecretUtils;
import com.epam.aidial.core.config.databind.JsonToStringDeserializer;
import com.epam.aidial.core.config.databind.StringToJsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

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

    public String toString() {
        return "Upstream(endpoint=" + this.getEndpoint() + ", responsesEndpoint=" + this.responsesEndpoint
                + ", key=" + SecretUtils.mask(this.getKey())
                + ", extraData=" + this.getExtraData()
                + ", secretExtraData=" + SecretUtils.mask(this.getSecretExtraData())
                + ", weight=" + this.getWeight()
                + ", tier=" + this.getTier() + ")";
    }
}