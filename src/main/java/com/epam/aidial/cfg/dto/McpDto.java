package com.epam.aidial.cfg.dto;

import lombok.Data;

import java.util.List;

@Data
public class McpDto {
    private String endpoint;
    private TransportDto transport = TransportDto.HTTP;
    private List<String> allowedTools = List.of();

    public enum TransportDto {
        HTTP
    }
}