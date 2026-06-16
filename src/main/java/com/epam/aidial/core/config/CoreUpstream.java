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

    public String toString() {
        return "Upstream(endpoint=" + this.getEndpoint() + ", responsesEndpoint=" + this.responsesEndpoint
                + ", key=" + SecretUtils.mask(this.getKey())
                + ", extraData=" + this.getExtraData()
                + ", secretExtraData=" + SecretUtils.mask(this.getSecretExtraData())
                + ", weight=" + this.getWeight()
                + ", tier=" + this.getTier() + ")";
    }
}