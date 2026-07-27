package com.epam.aidial.cfg.domain.model;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class CatalogSchema {

    private String schemaId;
    private String schema;
    private TypeEnum type;
    private String title;
    private String description;
    private CatalogEntityType catalogEntityType;
    private String catalogDisplayName;
    private String defaultLocale;
    private Map<String, String> defs;
    private Map<String, String> properties;
    private List<String> required;
    private Set<String> topics;
    private Long createdAt;
    private Long updatedAt;

    public enum TypeEnum {
        OBJECT,
        BOOLEAN
    }

    public enum CatalogEntityType {
        MODEL,
        AGENT,
        TOOLSET,
        SKILL,
        INTERCEPTOR
    }
}
