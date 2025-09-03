package com.epam.aidial.cfg.domain.service.mcp;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ToolSet.Transport;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@LogExecution
@RequiredArgsConstructor
public class McpClientFactory {

    public McpSyncClient create(String baseUrl, Transport transport) {
        var clientTransport = switch (transport) {
            case HTTP -> HttpClientStreamableHttpTransport.builder(baseUrl).build();
            case SSE -> HttpClientSseClientTransport.builder(baseUrl).build();
        };
        return McpClient.sync(clientTransport)
                .build();
    }
}
