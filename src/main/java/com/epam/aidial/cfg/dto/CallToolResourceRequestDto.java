package com.epam.aidial.cfg.dto;

import io.modelcontextprotocol.spec.McpSchema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CallToolResourceRequestDto {
    @NotNull
    private ResourcePathDto toolSetPath;
    @NotNull
    private McpSchema.CallToolRequest callToolRequest;
}
