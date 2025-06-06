package com.epam.aidial.cfg.dto;

import com.epam.aidial.cfg.dto.databind.JsonMapDeserializer;
import com.epam.aidial.cfg.dto.databind.JsonMapSerializer;
import com.epam.aidial.cfg.dto.validation.annotation.ApplicationTypeSchema;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@ApplicationTypeSchema
public class ApplicationTypeSchemaDto {

    @JsonProperty("$schema")
    private String schema = "https://dial.epam.com/application_type_schemas/schema#";

    @JsonProperty("$id")
    private String id;

    private String description;

    @JsonProperty("dial:applicationTypeEditorUrl")
    private String applicationTypeEditorUrl = "https://app_editor_url";

    @JsonProperty("dial:applicationTypeViewerUrl")
    private String applicationTypeViewerUrl;

    @JsonProperty("dial:applicationTypeDisplayName")
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
}
