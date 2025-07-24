package com.epam.aidial.cfg.dto.source;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "$type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = InterceptorEndpointsSourceDto.class, name = "endpoints"),
        @JsonSubTypes.Type(value = InterceptorRunnerSourceDto.class, name = "runner"),
        @JsonSubTypes.Type(value = InterceptorContainerSourceDto.class, name = "container")
})
public interface InterceptorSourceDto {
}
