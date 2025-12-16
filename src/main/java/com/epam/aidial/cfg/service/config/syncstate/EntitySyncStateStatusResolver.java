package com.epam.aidial.cfg.service.config.syncstate;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.EntitySyncStateStatus;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@LogExecution
public class EntitySyncStateStatusResolver {

    private final long thresholdMs;

    public EntitySyncStateStatusResolver(@Value("${config.export.sync.duration.thresholdMs}") long thresholdMs) {
        this.thresholdMs = thresholdMs;
    }

    public EntitySyncStateStatus resolve(JsonNode currentState,
                                         JsonNode configState,
                                         boolean isCurrentStateValid,
                                         long currentStateUpdatedAt,
                                         long configReloadTimestamp) {
        return isCurrentStateValid
                ? resolveStatusIfCurrentStateValid(currentState, configState, currentStateUpdatedAt, configReloadTimestamp)
                : resolveStatusIfCurrentStateInvalid(configState, currentStateUpdatedAt, configReloadTimestamp);
    }

    private EntitySyncStateStatus resolveStatusIfCurrentStateValid(JsonNode currentState,
                                                                   JsonNode configState,
                                                                   long currentStateUpdatedAt,
                                                                   long configReloadTimestamp) {
        if (configState == null) {
            return EntitySyncStateStatus.IN_PROGRESS;
        }

        if (currentState.equals(configState)) {
            return EntitySyncStateStatus.FULLY_SYNCED;
        }

        if (configReloadTimestamp - currentStateUpdatedAt > thresholdMs) {
            return EntitySyncStateStatus.IN_PROGRESS_TOO_LONG;
        }

        return EntitySyncStateStatus.IN_PROGRESS;
    }

    private EntitySyncStateStatus resolveStatusIfCurrentStateInvalid(JsonNode configState,
                                                                     long currentStateUpdatedAt,
                                                                     long configReloadTimestamp) {
        if (configState == null) {
            return EntitySyncStateStatus.FULLY_SYNCED;
        }

        if (configReloadTimestamp - currentStateUpdatedAt > thresholdMs) {
            return EntitySyncStateStatus.IN_PROGRESS_TOO_LONG;
        }

        return EntitySyncStateStatus.IN_PROGRESS;
    }
}
