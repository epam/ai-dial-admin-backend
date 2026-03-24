package com.epam.aidial.cfg.domain.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Mcp {
    private String endpoint;
    private Transport transport = Transport.HTTP;
    private List<String> allowedTools = new ArrayList<>();

    public enum Transport {
        HTTP
    }
}