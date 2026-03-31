package com.epam.aidial.core.config;

import com.epam.aidial.cfg.utils.SecretUtils;
import com.epam.aidial.core.config.databind.JsonToStringDeserializer;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
    This class conforms to application type schema's meta schema. Main class for reference is CoreUpstream.
    @see com.epam.aidial.core.config.CoreUpstream
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoreApplicationTypeSchemaUpstream {

    @JsonAlias({"endpoint", "dial:endpoint"})
    @JsonProperty("dial:endpoint")
    private String endpoint;

    @JsonAlias({"key", "dial:key"})
    @JsonProperty("dial:key")
    private String key;

    @JsonDeserialize(using = JsonToStringDeserializer.class)
    @JsonAlias({"extraData", "dial:extraData"})
    @JsonProperty("dial:extraData")
    private String extraData;

    @JsonAlias({"weight", "dial:weight"})
    @JsonProperty("dial:weight")
    private int weight = 1;

    @JsonAlias({"tier", "dial:tier"})
    @JsonProperty("dial:tier")
    private int tier = 0;

    public String toString() {
        return "Upstream(endpoint=" + this.getEndpoint() + ", key=" + SecretUtils.mask(this.getKey())
                + ", extraData=" + this.getExtraData() + ", weight=" + this.getWeight()
                + ", tier=" + this.getTier() + ")";
    }
}