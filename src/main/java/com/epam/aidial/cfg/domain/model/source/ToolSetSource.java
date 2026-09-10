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
        @JsonSubTypes.Type(value = ToolSetEndpointsSource.class, name = "endpoints"),
        @JsonSubTypes.Type(value = ToolSetContainerSource.class, name = "container"),
        @JsonSubTypes.Type(value = ToolSetMcpRegistrySource.class, name = "mcp-registry")
})
public abstract class ToolSetSource {
}
