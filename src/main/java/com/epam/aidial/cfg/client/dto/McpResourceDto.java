package com.epam.aidial.cfg.client.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class McpResourceDto {
    private String endpoint;
    @Builder.Default
    private TransportDto transport = TransportDto.HTTP;
    private List<String> allowedTools;
    @Builder.Default
    private McpConfigDeliveryDto configDelivery = McpConfigDeliveryDto.META;
    @Builder.Default
    private boolean forwardPerRequestKey = true;

    public enum TransportDto {
        HTTP
    }

    public enum McpConfigDeliveryDto {
        HEADER, META
    }
}