package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.databind.JsonMapDeserializer;
import com.epam.aidial.cfg.dto.databind.JsonMapSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

@Data
@NoArgsConstructor
public class CatalogSchemaDto {

    @JsonProperty("$schema")
    private String schema = "https://dial.epam.com/catalog_schemas/schema#";

    @JsonProperty("$id")
    @NotBlank(message = "Schema ID is required")
    private String id;

    private TypeDto type;
    private String title;
    private String description;

    @JsonProperty("dial:catalogEntityType")
    @NotNull(message = "Catalog entity type is required")
    private CatalogEntityTypeDto catalogEntityType;

    @JsonProperty("dial:catalogDisplayName")
    @NotBlank(message = "Catalog display name is required")
    private String catalogDisplayName;

    @JsonProperty("dial:defaultLocale")
    @Pattern(regexp = "^[a-z]{2}(-[A-Z]{2})?$", message = "Default locale must be a valid BCP-47 locale (e.g., 'en', 'en-US')")
    private String defaultLocale = "en";

    @JsonProperty("$defs")
    @JsonSerialize(using = JsonMapSerializer.class)
    @JsonDeserialize(using = JsonMapDeserializer.class)
    private Map<String, String> defs;

    @JsonSerialize(using = JsonMapSerializer.class)
    @JsonDeserialize(using = JsonMapDeserializer.class)
    private Map<String, String> properties;

    private List<String> required;

    private TreeSet<String> topics;

    @EqualsAndHashCode.Exclude
    private Instant createdAt;

    @EqualsAndHashCode.Exclude
    private Instant updatedAt;

    public enum TypeDto {
        OBJECT,
        BOOLEAN,
    }

    public enum CatalogEntityTypeDto {
        @JsonProperty("model")
        MODEL,
        @JsonProperty("agent")
        AGENT,
        @JsonProperty("toolset")
        TOOLSET,
        @JsonProperty("skill")
        SKILL,
        @JsonProperty("interceptor")
        INTERCEPTOR
    }

    public CatalogSchemaDto(CatalogSchemaDto other) {
        this.schema = other.schema;
        this.id = other.id;
        this.type = other.type;
        this.title = other.title;
        this.description = other.description;
        this.catalogEntityType = other.catalogEntityType;
        this.catalogDisplayName = other.catalogDisplayName;
        this.defaultLocale = other.defaultLocale;
        this.defs = other.defs != null ? new HashMap<>(other.defs) : null;
        this.properties = other.properties != null ? new HashMap<>(other.properties) : null;
        this.required = other.required != null ? new ArrayList<>(other.required) : null;
        this.topics = other.topics != null ? new TreeSet<>(other.topics) : null;
        this.createdAt = other.createdAt;
        this.updatedAt = other.updatedAt;
    }
}
