package com.epam.aidial.core.config;

import com.epam.aidial.cfg.utils.SecretUtils;
import com.epam.aidial.core.config.databind.JsonToStringDeserializer;
import com.epam.aidial.core.config.databind.StringToJsonSerializer;
import com.fasterxml.jackson.annotation.JsonAlias;
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
    @JsonAlias({"responsesEndpoint", "responses-endpoint", "dial:responsesEndpoint"})
    private String responsesEndpoint;  //0.43.0
    @JsonAlias({"key", "dial:key"})
    private String key;

    @JsonDeserialize(using = JsonToStringDeserializer.class)
    @JsonSerialize(using = StringToJsonSerializer.class)
    @JsonAlias({"extraData", "dial:extraData"})
    private String extraData;

    @JsonAlias({"weight", "dial:weight"})
    private int weight = 1;

    @JsonAlias({"tier", "dial:tier"})
    private int tier = 0;

    public String toString() {
        return "Upstream(endpoint=" + this.getEndpoint() + ", responsesEndpoint=" + this.responsesEndpoint
                + ", key=" + SecretUtils.mask(this.getKey())
                + ", extraData=" + this.getExtraData() + ", weight=" + this.getWeight()
                + ", tier=" + this.getTier() + ")";
    }
}