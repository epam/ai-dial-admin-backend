package com.epam.aidial.cfg.service.config.syncstate;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.EntitySyncStateStatus;
import com.epam.aidial.cfg.utils.json.JsonNodeComparator;
import com.fasterxml.jackson.databind.JsonNode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

@LogExecution
public class EntitySyncStateStatusResolver {

    private final Map<Class<?>, JsonNodeComparator> customJsonNodeComparators;
    private final long thresholdMs;

    public EntitySyncStateStatusResolver(Map<Class<?>, JsonNodeComparator> customJsonNodeComparators, long thresholdMs) {
        this.customJsonNodeComparators = customJsonNodeComparators;
        this.thresholdMs = thresholdMs;
    }

    public EntitySyncStateStatus resolve(@NotNull JsonNode currentState,
                                         @Nullable JsonNode configState,
                                         @NotNull Class<?> stateClass,
                                         boolean isCurrentStateValid,
                                         long currentStateUpdatedAt,
                                         long configReloadTimestamp) {

        if ((isCurrentStateValid && areStatesEqual(currentState, configState, stateClass)) || (!isCurrentStateValid && configState == null)) {
            return EntitySyncStateStatus.FULLY_SYNCED;
        }

        return resolveInProgressStatus(currentStateUpdatedAt, configReloadTimestamp);
    }

    private boolean areStatesEqual(JsonNode currentState, JsonNode configState, Class<?> stateClass) {
        JsonNodeComparator customJsonNodeComparator = customJsonNodeComparators.get(stateClass);
        return customJsonNodeComparator != null
                ? customJsonNodeComparator.equals(currentState, configState)
                : currentState.equals(configState);
    }

    private EntitySyncStateStatus resolveInProgressStatus(long currentStateUpdatedAt, long configReloadTimestamp) {
        return (configReloadTimestamp - currentStateUpdatedAt > thresholdMs)
                ? EntitySyncStateStatus.IN_PROGRESS_TOO_LONG
                : EntitySyncStateStatus.IN_PROGRESS;
    }
}
