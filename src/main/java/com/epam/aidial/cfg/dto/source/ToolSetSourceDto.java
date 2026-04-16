package com.epam.aidial.cfg.dto.source;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "$type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = ToolSetEndpointsSourceDto.class, name = "endpoints"),
        @JsonSubTypes.Type(value = ToolSetContainerSourceDto.class, name = "container"),
        @JsonSubTypes.Type(value = ToolSetMcpRegistrySourceDto.class, name = "mcp-registry")
})
public interface ToolSetSourceDto {
}
