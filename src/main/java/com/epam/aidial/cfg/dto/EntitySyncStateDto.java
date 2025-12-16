package com.epam.aidial.cfg.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class EntitySyncStateDto {

    private JsonNode currentState;
    private JsonNode configState;
    private EntitySyncStateStatusDto status;
}
