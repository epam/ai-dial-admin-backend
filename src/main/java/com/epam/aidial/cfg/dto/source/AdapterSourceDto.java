package com.epam.aidial.cfg.dto.source;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "$type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = AdapterEndpointsSourceDto.class, name = "endpoints"),
        @JsonSubTypes.Type(value = AdapterContainerSourceDto.class, name = "container")
})
public interface AdapterSourceDto {
}
