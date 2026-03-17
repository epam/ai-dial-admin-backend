package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.List;

@Data
public class McpResourceDto {
    private String endpoint;
    private TransportDto transport = TransportDto.HTTP;
    private List<String> allowedTools;

    public enum TransportDto {
        HTTP
    }
}