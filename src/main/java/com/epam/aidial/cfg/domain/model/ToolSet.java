package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.domain.model.source.ToolSetSource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ToolSet extends SecuredRoleBased {

    private String endpoint;
    private String iconUrl;
    private String description;
    private String displayName;
    private List<String> descriptionKeywords;
    private Integer maxRetryAttempts;
    private ToolSetSource source;
    private String author;
    private Long createdAt;
    private Long updatedAt;

    private Transport transport;
    private List<String> allowedTools = List.of();

    public enum Transport {
        HTTP, SSE
    }
}
