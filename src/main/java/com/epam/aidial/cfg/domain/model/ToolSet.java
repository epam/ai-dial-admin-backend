package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.domain.model.source.ToolSetSource;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
public class ToolSet extends SecuredRoleBased {

    private String endpoint;
    private String iconUrl;
    private String description;
    private String displayName;
    private LinkedHashSet<String> descriptionKeywords;
    private Integer maxRetryAttempts;
    private ToolSetSource source;
    private String author;
    private Long createdAt;
    private Long updatedAt;

    private Transport transport;
    private List<String> allowedTools = new ArrayList<>();

    public enum Transport {
        HTTP, SSE
    }
}
