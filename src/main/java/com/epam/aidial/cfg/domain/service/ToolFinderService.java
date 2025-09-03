package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ToolSet.Transport;
import com.epam.aidial.cfg.domain.service.mcp.McpClientFactory;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@LogExecution
@RequiredArgsConstructor
public class ToolFinderService {

    private final McpClientFactory mcpClientFactory;

    public McpSchema.ListToolsResult findTools(String endpoint, Transport transport, String nextCursor) {
        try (var mcpClient = mcpClientFactory.create(endpoint, transport)) {
            mcpClient.initialize();
            return mcpClient.listTools(nextCursor);
        } catch (Exception e) {
            String reason = e.getCause() != null ? e.getCause().getMessage() : e.getMessage();
            String message = "Failed to retrieve a list of tools from MCP server. Base URL: %s. Transport: %s. Reason: %s"
                    .formatted(endpoint, transport, reason);
            log.error(message, e);
            throw new RuntimeException(message);
        }
    }
}