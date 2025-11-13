package com.epam.aidial.core.config;

import com.epam.aidial.core.config.databind.JsonArrayToSchemaMapDeserializer;
import com.epam.aidial.core.config.databind.MapToJsonArraySerializer;
import com.epam.aidial.core.config.validation.ConformToMetaSchema;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Data;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Config {
    public static final String ASSISTANT = "assistant";

    // maintain the order of routes defined in the config
    private LinkedHashMap<String, CoreRoute> routes = new LinkedHashMap<>();
    private Map<String, CoreModel> models = new HashMap<>();
    private Map<String, CoreAddon> addons = new HashMap<>();
    private Map<String, CoreApplication> applications = new HashMap<>();
    private Map<String, CoreToolSet> toolsets = new HashMap<>(); // 0.32.0
    private Assistants assistant = new Assistants();
    private Map<String, CoreKey> keys = new HashMap<>();
    private Map<String, CoreRole> roles = new HashMap<>();
    private Set<Integer> retriableErrorCodes = new HashSet<>();
    private Map<String, CoreInterceptor> interceptors = new HashMap<>();

    @JsonDeserialize(using = JsonArrayToSchemaMapDeserializer.class)
    @JsonSerialize(using = MapToJsonArraySerializer.class)
    @ConformToMetaSchema(message = "All custom application type schemas should conform to meta schema")
    private Map<String, String> applicationTypeSchemas = new HashMap<>();

}
