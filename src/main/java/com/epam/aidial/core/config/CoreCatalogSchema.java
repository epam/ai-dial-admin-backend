package com.epam.aidial.core.config;

import com.epam.aidial.cfg.dto.databind.JsonMapDeserializer;
import com.epam.aidial.cfg.dto.databind.JsonMapSerializer;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CoreCatalogSchema {

    @JsonProperty("$schema")
    private String schema;

    @JsonProperty("$id")
    private String id;

    private CoreType type;
    private String title;
    private String description;

    @JsonProperty("dial:catalogEntityType")
    private CatalogEntityType catalogEntityType;

    @JsonProperty("dial:catalogDisplayName")
    private String catalogDisplayName;

    @JsonProperty("dial:defaultLocale")
    private String defaultLocale;

    @JsonProperty("$defs")
    @JsonSerialize(using = JsonMapSerializer.class)
    @JsonDeserialize(using = JsonMapDeserializer.class)
    private Map<String, String> defs;

    @JsonSerialize(using = JsonMapSerializer.class)
    @JsonDeserialize(using = JsonMapDeserializer.class)
    private Map<String, String> properties;

    private List<String> required;

    public enum CoreType {
        @JsonAlias("OBJECT")
        OBJECT,
        @JsonAlias("BOOLEAN")
        BOOLEAN,
    }

    public enum CatalogEntityType {
        @JsonAlias("MODEL")
        @JsonProperty("MODEL")
        MODEL,
        @JsonAlias("AGENT")
        @JsonProperty("AGENT")
        AGENT,
        @JsonAlias("TOOLSET")
        @JsonProperty("TOOLSET")
        TOOLSET,
        @JsonAlias("SKILL")
        @JsonProperty("SKILL")
        SKILL,
        @JsonAlias("INTERCEPTOR")
        @JsonProperty("INTERCEPTOR")
        INTERCEPTOR
    }
}
