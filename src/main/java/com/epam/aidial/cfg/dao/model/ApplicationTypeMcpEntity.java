package com.epam.aidial.cfg.dao.model;

import com.epam.aidial.cfg.dao.converter.StringNullableListJsonConverter;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

import java.util.List;

@Data
@Embeddable
public class ApplicationTypeMcpEntity {
    private String endpoint;
    @Enumerated(EnumType.STRING)
    private TransportEntity transport;
    @Convert(converter = StringNullableListJsonConverter.class)
    private List<String> allowedTools;
    @Enumerated(EnumType.STRING)
    private McpConfigDeliveryEntity configDelivery;
    private Boolean forwardPerRequestKey;

    public enum McpConfigDeliveryEntity {
        HEADER, META
    }

    public enum TransportEntity {
        HTTP
    }
}