package com.epam.aidial.cfg.dto.source;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "$type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ModelEndpointsSourceDto.class, name = "endpoints"),
        @JsonSubTypes.Type(value = ModelContainerSourceDto.class, name = "container"),
        @JsonSubTypes.Type(value = AdapterSourceDto.class, name = "adapter"),
})
public interface ModelSourceDto {
}
