package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.utils.SecretUtils;
import lombok.Data;

@Data
public class Upstream {

    private Long id;
    private String endpoint;
    private String key;
    private String extraData;
    private int weight;
    private int tier;

    public String toString() {
        return "Upstream(endpoint=" + this.getEndpoint() + ", key=" + SecretUtils.mask(this.getKey())
                + ", extraData=" + this.getExtraData() + ", weight=" + this.getWeight()
                + ", tier=" + this.getTier() + ")";
    }
}