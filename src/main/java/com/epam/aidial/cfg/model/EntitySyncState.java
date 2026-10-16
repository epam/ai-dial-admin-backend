package com.epam.aidial.cfg.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import tools.jackson.databind.JsonNode;

@Data
@AllArgsConstructor
public class EntitySyncState {

    private JsonNode currentState;
    private JsonNode configState;
    private EntitySyncStateStatus status;

    public static EntitySyncState unknown() {
        return new EntitySyncState(null, null, EntitySyncStateStatus.UNKNOWN);
    }
}
