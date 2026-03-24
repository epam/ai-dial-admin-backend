package com.epam.aidial.cfg.dao.model;

import com.epam.aidial.cfg.dao.converter.StringNullableListJsonConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.util.List;

@Data
@Embeddable
public class McpEntity {
    @Column(name = "mcp_endpoint")
    private String endpoint;
    @Enumerated(EnumType.STRING)
    @Column(name = "mcp_transport")
    private TransportEntity transport;
    @Convert(converter = StringNullableListJsonConverter.class)
    @Column(name = "mcp_allowed_tools")
    private List<String> allowedTools;

    public enum TransportEntity {
        HTTP
    }
}