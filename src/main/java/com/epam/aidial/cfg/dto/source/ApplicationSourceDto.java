package com.epam.aidial.cfg.dto.source;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "$type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ApplicationEndpointsSourceDto.class, name = "endpoints"),
        @JsonSubTypes.Type(value = ApplicationSchemaSourceDto.class, name = "schema"),
        @JsonSubTypes.Type(value = ApplicationContainerSourceDto.class, name = "container"),
})
public interface ApplicationSourceDto {
}
