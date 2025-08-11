package com.epam.aidial.cfg.domain.model;

import com.epam.aidial.cfg.dto.databind.JsonMapDeserializer;
import com.epam.aidial.cfg.dto.databind.JsonMapSerializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
public class ApplicationTypeSchema {

    private String schemaId;
    private String schema;
    private Type type;
    private String title;
    private String description;
    private String applicationTypeEditorUrl;
    private String applicationTypeViewerUrl;
    private String applicationTypeDisplayName;
    private String applicationTypeCompletionEndpoint;
    private String applicationTypeConfigurationEndpoint;
    private String applicationTypeRateEndpoint;
    private String applicationTypeTokenizeEndpoint;
    private String applicationTypeTruncatePromptEndpoint;
    private Boolean appendApplicationPropertiesHeader;
    private String applicationTypeIconUrl;
    private List<String> applicationTypeRoutes;
    private Boolean applicationTypePlaybackSupport;

    @JsonProperty("$defs")
    @JsonSerialize(using = JsonMapSerializer.class)
    @JsonDeserialize(using = JsonMapDeserializer.class)
    private Map<String, String> defs;

    @JsonSerialize(using = JsonMapSerializer.class)
    @JsonDeserialize(using = JsonMapDeserializer.class)
    private Map<String, String> properties;

    private List<String> required;
    private List<String> applications;
    private Set<String> topics;
    private Long createdAt;
    private Long updatedAt;

    public enum Type {
        OBJECT,
        BOOLEAN,
    }
}
