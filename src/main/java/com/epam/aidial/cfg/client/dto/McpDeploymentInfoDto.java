package com.epam.aidial.cfg.client.dto;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
@NoArgsConstructor
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class McpDeploymentInfoDto extends DeploymentInfoDto {
    @NotNull
    private McpTransport transport = McpTransport.HTTP_STREAMING;

    public McpDeploymentInfoDto(String id, String name, String url, McpTransport transport) {
        super(id, name, url);
        this.transport = transport;
    }

    public enum McpTransport {
        HTTP_STREAMING, SSE
    }
}

