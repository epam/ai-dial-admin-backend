package com.epam.aidial.cfg.dto;

import lombok.Data;
import tools.jackson.databind.JsonNode;

@Data
public class EntitySyncStateDto {

    private JsonNode currentState;
    private JsonNode configState;
    private EntitySyncStateStatusDto status;
}
