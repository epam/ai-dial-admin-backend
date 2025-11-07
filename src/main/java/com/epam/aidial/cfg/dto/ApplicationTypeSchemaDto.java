package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.databind.JsonMapDeserializer;
import com.epam.aidial.cfg.dto.databind.JsonMapSerializer;
import com.epam.aidial.cfg.dto.route.DependentRouteDto;
import com.epam.aidial.cfg.dto.validation.annotation.ApplicationTypeSchema;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@NoArgsConstructor
@ApplicationTypeSchema
public class ApplicationTypeSchemaDto {

    @JsonProperty("$schema")
    private String schema = "https://dial.epam.com/application_type_schemas/schema#";

    @JsonProperty("$id")
    private String id;

    private TypeDto type;
    private String title;
    private String description;

    @JsonProperty("dial:applicationTypeEditorUrl")
    private String applicationTypeEditorUrl;

    @JsonProperty("dial:applicationTypeViewerUrl")
    private String applicationTypeViewerUrl;

    @JsonProperty("dial:applicationTypeDisplayName")
    @NotBlank(message = "ApplicationTypeDisplayName is required")
    private String applicationTypeDisplayName;

    @JsonProperty("dial:applicationTypeCompletionEndpoint")
    private String applicationTypeCompletionEndpoint = "https://app_hostname/openai/deployments/app_name/chat/completions";

    @JsonProperty("dial:applicationTypeConfigurationEndpoint")
    private String applicationTypeConfigurationEndpoint;

    @JsonProperty("dial:applicationTypeRateEndpoint")
    private String applicationTypeRateEndpoint;

    @JsonProperty("dial:applicationTypeTokenizeEndpoint")
    private String applicationTypeTokenizeEndpoint;

    @JsonProperty("dial:applicationTypeTruncatePromptEndpoint")
    private String applicationTypeTruncatePromptEndpoint;

    @JsonProperty("dial:appendApplicationPropertiesHeader")
    private Boolean appendApplicationPropertiesHeader;

    @JsonProperty("dial:applicationTypeIconUrl")
    private String applicationTypeIconUrl;

    @JsonProperty("dial:applicationTypeRoutes")
    private List<DependentRouteDto> applicationTypeRoutes;

    @JsonProperty("dial:applicationTypePlaybackSupport")
    private Boolean applicationTypePlaybackSupport;

    @JsonProperty("dial:applicationTypeBucketCopy")
    private CopyAppBucketOptionsDto applicationTypeBucketCopy;

    @JsonProperty("dial:applicationTypeInterceptors")
    private List<String> applicationTypeInterceptors;

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
    @EqualsAndHashCode.Exclude
    private Instant createdAt;
    @EqualsAndHashCode.Exclude
    private Instant updatedAt;

    public enum TypeDto {
        OBJECT,
        BOOLEAN,
    }

    public enum CopyAppBucketOptionsDto {
        @JsonProperty("ENABLED")
        ENABLED,
        @JsonProperty("DISABLED")
        DISABLED,
    }

    public ApplicationTypeSchemaDto(ApplicationTypeSchemaDto other) {
        this.schema = other.schema;
        this.id = other.id;
        this.type = other.type;
        this.title = other.title;
        this.description = other.description;
        this.applicationTypeEditorUrl = other.applicationTypeEditorUrl;
        this.applicationTypeViewerUrl = other.applicationTypeViewerUrl;
        this.applicationTypeDisplayName = other.applicationTypeDisplayName;
        this.applicationTypeCompletionEndpoint = other.applicationTypeCompletionEndpoint;
        this.applicationTypeConfigurationEndpoint = other.applicationTypeConfigurationEndpoint;
        this.applicationTypeRateEndpoint = other.applicationTypeRateEndpoint;
        this.applicationTypeTokenizeEndpoint = other.applicationTypeTokenizeEndpoint;
        this.applicationTypeTruncatePromptEndpoint = other.applicationTypeTruncatePromptEndpoint;
        this.appendApplicationPropertiesHeader = other.appendApplicationPropertiesHeader;
        this.applicationTypeIconUrl = other.applicationTypeIconUrl;
        this.applicationTypeRoutes = other.applicationTypeRoutes != null ? new ArrayList<>(other.applicationTypeRoutes) : null;
        this.applicationTypePlaybackSupport = other.applicationTypePlaybackSupport;
        this.applicationTypeBucketCopy = other.applicationTypeBucketCopy;
        this.defs = other.defs != null ? new HashMap<>(other.defs) : null;
        this.properties = other.properties != null ? new HashMap<>(other.properties) : null;
        this.required = other.required != null ? new ArrayList<>(other.required) : null;
        this.applications = other.applications != null ? new ArrayList<>(other.applications) : null;
        this.topics = other.topics != null ? new HashSet<>(other.topics) : null;
        this.createdAt = other.createdAt;
        this.updatedAt = other.updatedAt;
    }
}
