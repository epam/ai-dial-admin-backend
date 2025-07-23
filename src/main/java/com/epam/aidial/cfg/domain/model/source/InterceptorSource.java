package com.epam.aidial.cfg.domain.model.source;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "$type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = InterceptorEndpointsSource.class, name = "endpoints"),
        @JsonSubTypes.Type(value = InterceptorRunnerSource.class, name = "template"),
        @JsonSubTypes.Type(value = InterceptorContainerSource.class, name = "container")
})
public abstract class InterceptorSource {
}
