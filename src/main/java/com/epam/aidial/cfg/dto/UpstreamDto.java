package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.validation.annotation.Endpoint;
import com.epam.aidial.cfg.dto.validation.annotation.UpstreamEndpoint;
import com.epam.aidial.cfg.utils.SecretUtils;
import com.epam.aidial.core.config.databind.JsonToStringDeserializer;
import com.epam.aidial.core.config.databind.StringToJsonSerializer;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.Data;
import lombok.NoArgsConstructor;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.util.Map;

@Data
@NoArgsConstructor
public class UpstreamDto {

    private String id;
    @UpstreamEndpoint
    private String endpoint;
    @Endpoint
    private String responsesEndpoint;
    private String key;
    @JsonDeserialize(using = JsonToStringDeserializer.class)
    @JsonSerialize(using = StringToJsonSerializer.class)
    private String extraData;
    @JsonSetter(nulls = Nulls.SKIP)
    private int weight = 1;
    @Min(value = 0, message = "Tier must be a non-negative number")
    private int tier = 0;
    @JsonDeserialize(using = JsonToStringDeserializer.class)
    @JsonSerialize(using = StringToJsonSerializer.class)
    private String secretExtraData;
    @UpstreamEndpoint
    private String baseUrl;
    @Valid
    private Map<String, UpstreamInterfaceDto> interfaces = Map.of();
    private UpstreamAuthConfigDto authType = UpstreamAuthConfigDto.of(UpstreamAuthTypeDto.API_KEY);

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