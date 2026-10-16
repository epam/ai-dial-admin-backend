package com.epam.aidial.cfg.domain.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Map;

@Data
public class ExternalSchema {
    @JsonProperty("$defs")
    private Map<String, JsonNode> defs;
    private Map<String, JsonNode> properties;
    private List<String> required;
}