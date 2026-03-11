package com.epam.aidial.cfg.client.mcp;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ToolSet.Transport;
import com.epam.aidial.cfg.utils.NullSafeUtils;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.HttpClientSseClientTransport;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpRequest;
import java.util.Map;

@Component
@LogExecution
@RequiredArgsConstructor
public class McpClientFactory {

    @SneakyThrows
    public McpSyncClient create(String mcpEndpoint, Transport transport, Map<String, String> customHeaders) {
        var uri = new URI(mcpEndpoint);
        var baseUrl = uri.getScheme() + "://" + uri.getAuthority();
        var relativePath = uri.getRawPath();

        HttpRequest.Builder requestBuilder = null;

        if (customHeaders != null) {
            requestBuilder = NullSafeUtils.createIfNull(requestBuilder, HttpRequest::newBuilder);
            for (var headerKv : customHeaders.entrySet()) {
                requestBuilder = requestBuilder.header(headerKv.getKey(), headerKv.getValue());
            }
        }

        var clientTransport = switch (transport) {
            case HTTP -> {
                var builder = HttpClientStreamableHttpTransport.builder(baseUrl);
                if (StringUtils.isNotBlank(relativePath)) {
                    builder = builder.endpoint(relativePath);
                }
                if (requestBuilder != null) {
                    builder = builder.requestBuilder(requestBuilder);
                }
                yield builder.build();
            }
            case SSE -> {
                var builder = HttpClientSseClientTransport.builder(baseUrl);
                if (StringUtils.isNotBlank(relativePath)) {
                    builder = builder.sseEndpoint(relativePath);
                }
                if (requestBuilder != null) {
                    builder = builder.requestBuilder(requestBuilder);
                }
                yield builder.build();
            }
        };

        return McpClient.sync(clientTransport).build();
    }
}