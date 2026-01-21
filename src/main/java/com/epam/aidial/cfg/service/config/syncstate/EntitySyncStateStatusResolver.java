package com.epam.aidial.cfg.service.config.syncstate;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.EntitySyncStateStatus;
import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@LogExecution
public class EntitySyncStateStatusResolver {

    private final long thresholdMs;

    public EntitySyncStateStatusResolver(@Value("${config.export.sync.duration.thresholdMs}") long thresholdMs) {
        this.thresholdMs = thresholdMs;
    }

    public EntitySyncStateStatus resolve(@NotNull JsonNode currentState,
                                         @Nullable JsonNode configState,
                                         boolean isCurrentStateValid,
                                         long currentStateUpdatedAt,
                                         long configReloadTimestamp) {
        if ((isCurrentStateValid && currentState.equals(configState)) || (!isCurrentStateValid && configState == null)) {
            return EntitySyncStateStatus.FULLY_SYNCED;
        }

        return resolveInProgressStatus(currentStateUpdatedAt, configReloadTimestamp);
    }

    private EntitySyncStateStatus resolveInProgressStatus(long currentStateUpdatedAt, long configReloadTimestamp) {
        return (configReloadTimestamp - currentStateUpdatedAt > thresholdMs)
                ? EntitySyncStateStatus.IN_PROGRESS_TOO_LONG
                : EntitySyncStateStatus.IN_PROGRESS;
    }
}
