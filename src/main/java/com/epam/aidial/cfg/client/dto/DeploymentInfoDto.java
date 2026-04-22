package com.epam.aidial.cfg.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        property = "$type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = McpDeploymentInfoDto.class, name = "mcp"),
        @JsonSubTypes.Type(value = AdapterDeploymentInfoDto.class, name = "adapter"),
        @JsonSubTypes.Type(value = ApplicationDeploymentInfoDto.class, name = "application"),
        @JsonSubTypes.Type(value = InterceptorDeploymentInfoDto.class, name = "interceptor"),
        @JsonSubTypes.Type(value = NimDeploymentInfoDto.class, name = "nim"),
        @JsonSubTypes.Type(value = InferenceDeploymentInfoDto.class, name = "inference"),
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class DeploymentInfoDto {
    @NotNull
    private String id;
    @NotNull
    private String displayName;
    @Nullable
    private String url;
}