package com.epam.aidial.cfg.domain.service;

import com.epam.aidial.cfg.client.mcp.McpClientFactory;
import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.domain.model.ToolSet.Transport;
import io.modelcontextprotocol.spec.McpSchema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@LogExecution
@RequiredArgsConstructor
public class ToolCallService {

    private final McpClientFactory mcpClientFactory;

    public McpSchema.CallToolResult callTool(String endpoint,
                                             Transport transport,
                                             Map<String, String> customHeaders,
                                             McpSchema.CallToolRequest callToolRequest) {
        try (var mcpClient = mcpClientFactory.create(endpoint, transport, customHeaders)) {
            mcpClient.initialize();
            return mcpClient.callTool(callToolRequest);
        } catch (Exception e) {
            String reason = ExceptionUtils.getRootCause(e).getMessage();
            String message = "Failed to call a tool via MCP server. Base URL: %s. Transport: %s. Reason: %s"
                    .formatted(endpoint, transport, reason);
            log.warn(message, e);
            throw new RuntimeException(message);
        }
    }
}