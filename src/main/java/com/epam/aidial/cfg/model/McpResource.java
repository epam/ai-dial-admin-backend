package com.epam.aidial.cfg.model;

import lombok.Data;

import java.util.List;

@Data
public class McpResource {
    private String endpoint;
    private Transport transport = Transport.HTTP;
    private List<String> allowedTools;

    public enum Transport {
        HTTP
    }
}