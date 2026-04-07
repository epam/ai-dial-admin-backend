package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.validation.annotation.Endpoint;
import com.epam.aidial.cfg.dto.validation.annotation.UpstreamEndpoint;
import com.epam.aidial.cfg.utils.SecretUtils;
import com.epam.aidial.core.config.databind.JsonToStringDeserializer;
import com.epam.aidial.core.config.databind.StringToJsonSerializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpstreamDto {

    @UpstreamEndpoint
    private String endpoint;
    @Endpoint
    private String responsesEndpoint;
    private String key;
    @JsonDeserialize(using = JsonToStringDeserializer.class)
    @JsonSerialize(using = StringToJsonSerializer.class)
    private String extraData;
    private int weight = 1;
    @Min(value = 0, message = "Tier must be a non-negative number")
    private int tier = 0;

    public String toString() {
        return "Upstream(endpoint=" + this.getEndpoint() + ", responsesEndpoint=" + this.responsesEndpoint
                + ", key=" + SecretUtils.mask(this.getKey())
                + ", extraData=" + this.getExtraData() + ", weight=" + this.getWeight()
                + ", tier=" + this.getTier() + ")";
    }
}