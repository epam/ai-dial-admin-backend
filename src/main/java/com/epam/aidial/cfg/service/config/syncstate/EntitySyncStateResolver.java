package com.epam.aidial.cfg.service.config.syncstate;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.model.EntitySyncState;
import com.epam.aidial.cfg.model.EntitySyncStateStatus;
import com.epam.aidial.cfg.service.config.reload.CoreConfigReloadCache;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
@LogExecution
public class EntitySyncStateResolver {

    private final ObjectMapper objectMapper;
    private final CoreConfigReloadCache coreConfigReloadCache;
    private final EntitySyncStateStatusResolver syncStateStatusResolver;

    public <T> EntitySyncState resolve(T currentState,
                                       long currentStateUpdatedAt,
                                       String configEntitiesType,
                                       String entityKeyInConfig) {
        return resolve(currentState, true, currentStateUpdatedAt, configEntitiesType, entityKeyInConfig);
    }

    public <T> EntitySyncState resolve(T currentState,
                                       boolean isCurrentStateValid,
                                       long currentStateUpdatedAt,
                                       String entityType,
                                       String entityKeyInConfig) {
        CoreConfigReloadCache.Entry cacheEntry = coreConfigReloadCache.get();
        log.debug("Current state: {}, is current state valid: {}, current state updated at: {}, entity type: {}, entity key in config: {}, cache: {}",
                currentState, isCurrentStateValid, currentStateUpdatedAt, entityType, entityKeyInConfig, cacheEntry);

        if (cacheEntry == null) {
            log.info("Config reload cache is empty");
            return EntitySyncState.unknown();
        }

        JsonNode config = cacheEntry.config();
        if (config == null) {
            log.info("Config in config reload cache is missing. Cache: {}", cacheEntry);
            return EntitySyncState.unknown();
        }

        JsonNode entitiesJsonNode = config.get(entityType);
        if (entitiesJsonNode == null) {
            log.warn("Config doesn't have '{}' property. Cache: {}", entityType, cacheEntry);
            return EntitySyncState.unknown();
        }

        JsonNode configStateJsonNode = entitiesJsonNode.get(entityKeyInConfig);
        JsonNode currentStateJsonNode = objectMapper.valueToTree(currentState);

        EntitySyncStateStatus syncStateStatus = syncStateStatusResolver.resolve(
                currentStateJsonNode,
                configStateJsonNode,
                isCurrentStateValid,
                currentStateUpdatedAt,
                cacheEntry.reloadTimestamp()
        );

        currentStateJsonNode = isCurrentStateValid ? currentStateJsonNode : null;

        return new EntitySyncState(currentStateJsonNode, configStateJsonNode, syncStateStatus);
    }
}
