package com.epam.aidial.cfg.client.mcp;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ToolSet.Transport;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.net.URI;

@Component
@LogExecution
@RequiredArgsConstructor
public class McpClientFactory {

    @SneakyThrows
    public McpSyncClient create(String mcpEndpoint, Transport transport) {
        var uri = new URI(mcpEndpoint);
        var baseUrl = uri.getScheme() + "://" + uri.getAuthority();
        var relativePath = uri.getPath();

        var clientTransport = switch (transport) {
            case HTTP -> {
                var builder = HttpClientStreamableHttpTransport.builder(baseUrl);
                if (StringUtils.isNotBlank(relativePath)) {
                    builder = builder.endpoint(relativePath);
                }
                yield builder.build();
            }
            case SSE -> {
                var builder = HttpClientSseClientTransport.builder(baseUrl);
                if (StringUtils.isNotBlank(relativePath)) {
                    builder = builder.sseEndpoint(relativePath);
                }
                yield builder.build();
            }
        };

        return McpClient.sync(clientTransport)
                .build();
    }
}
