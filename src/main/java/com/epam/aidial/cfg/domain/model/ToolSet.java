package com.epam.aidial.cfg.domain.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ToolSet extends RoleBased {

    private Transport transport;
    private List<String> allowedTools = List.of();

    private Long createdAt;
    private Long updatedAt;

    public enum Transport {
        HTTP, SSE
    }
}
