package com.epam.aidial.cfg.model;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Data;

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
