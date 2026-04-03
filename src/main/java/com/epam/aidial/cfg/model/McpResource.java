package com.epam.aidial.cfg.model;

import lombok.Data;

import java.util.List;

@Data
public class McpResource {
    private String endpoint;
    private Transport transport = Transport.HTTP;
    private List<String> allowedTools;
    private McpConfigDelivery configDelivery = McpConfigDelivery.META;
    private boolean forwardPerRequestKey = true;

    public enum Transport {
        HTTP
    }

    public enum McpConfigDelivery {
        HEADER, META
    }
}