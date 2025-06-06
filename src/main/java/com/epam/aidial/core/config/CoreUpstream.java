package com.epam.aidial.core.config;

import com.epam.aidial.cfg.utils.SecretUtils;
import com.epam.aidial.core.config.databind.JsonToStringDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CoreUpstream {

    private String endpoint;
    private String key;
    @JsonDeserialize(using = JsonToStringDeserializer.class)
    private String extraData;
    private int weight = 1;
    private int tier = 0;

    public String toString() {
        return "Upstream(endpoint=" + this.getEndpoint() + ", key=" + SecretUtils.mask(this.getKey())
                + ", extraData=" + this.getExtraData() + ", weight=" + this.getWeight()
                + ", tier=" + this.getTier() + ")";
    }
}