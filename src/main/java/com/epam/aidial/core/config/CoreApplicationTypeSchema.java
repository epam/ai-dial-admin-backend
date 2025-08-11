package com.epam.aidial.core.config;

import com.epam.aidial.cfg.dto.databind.JsonMapDeserializer;
import com.epam.aidial.cfg.dto.databind.JsonMapSerializer;
import com.epam.aidial.cfg.dto.validation.annotation.ApplicationTypeSchema;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Data
@ApplicationTypeSchema
public class CoreApplicationTypeSchema {

    @JsonProperty("$schema")
    private String schema;

    @JsonProperty("$id")
    private String id;

    private CoreType type;
    private String title;
    private String description;

    @JsonProperty("dial:applicationTypeEditorUrl")
    private String applicationTypeEditorUrl;

    @JsonProperty("dial:applicationTypeViewerUrl")
    private String applicationTypeViewerUrl;

    @JsonProperty("dial:applicationTypeDisplayName")
    private String applicationTypeDisplayName;

    @JsonProperty("dial:applicationTypeCompletionEndpoint")
    private String applicationTypeCompletionEndpoint;

    @JsonProperty("dial:applicationTypeConfigurationEndpoint")
    private String applicationTypeConfigurationEndpoint; // 0.26.0

    @JsonProperty("dial:applicationTypeRateEndpoint")
    private String applicationTypeRateEndpoint; // 0.26.0

    @JsonProperty("dial:applicationTypeTokenizeEndpoint")
    private String applicationTypeTokenizeEndpoint; // 0.26.0

    @JsonProperty("dial:applicationTypeTruncatePromptEndpoint")
    private String applicationTypeTruncatePromptEndpoint; // 0.26.0

    @JsonProperty("dial:appendApplicationPropertiesHeader")
    private Boolean appendApplicationPropertiesHeader; // 0.26.0

    @JsonProperty("dial:applicationTypeIconUrl")
    private String applicationTypeIconUrl; // 0.29.0

    @JsonProperty("dial:applicationTypeRoutes")
    private LinkedHashMap<String, CoreApplicationTypeSchemaRoute> applicationTypeRoutes; // 0.33.0

    // TODO [VPA]: create schema 0.34.0 after it's release
    @JsonProperty("dial:applicationTypePlaybackSupport")
    private Boolean applicationTypePlaybackSupport; // 0.34.0

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
}
