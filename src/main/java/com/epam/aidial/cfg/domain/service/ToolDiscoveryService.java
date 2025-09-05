package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.client.mcp.McpClientFactory;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ToolSet.Transport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@LogExecution
@RequiredArgsConstructor
public class ToolDiscoveryService {

    private final McpClientFactory mcpClientFactory;

    public McpSchema.ListToolsResult discoverTools(String endpoint, Transport transport, String nextCursor) {
        try (var mcpClient = mcpClientFactory.create(endpoint, transport)) {
            mcpClient.initialize();
            return mcpClient.listTools(nextCursor);
        } catch (Exception e) {
            String reason = ExceptionUtils.getRootCause(e).getMessage();
            String message = "Failed to retrieve a list of tools from MCP server. Base URL: %s. Transport: %s. Reason: %s"
                    .formatted(endpoint, transport, reason);
            log.warn(message, e);
            throw new RuntimeException(message);
        }
    }
}