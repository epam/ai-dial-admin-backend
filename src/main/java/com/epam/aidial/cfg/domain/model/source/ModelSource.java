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
        @JsonSubTypes.Type(value = ModelEndpointsSource.class, name = "endpoints"),
        @JsonSubTypes.Type(value = ModelContainerSource.class, name = "container"),
        @JsonSubTypes.Type(value = ModelAdapterSource.class, name = "adapter"),
})
public abstract class ModelSource {
}
