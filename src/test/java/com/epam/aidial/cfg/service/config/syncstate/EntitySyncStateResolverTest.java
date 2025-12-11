package com.epam.aidial.cfg.service.config.syncstate;

import com.epam.aidial.cfg.model.EntitySyncState;
import com.epam.aidial.cfg.model.EntitySyncStateStatus;
import com.epam.aidial.cfg.service.config.reload.CoreConfigReloadCache;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntitySyncStateResolverTest {

    private static final ObjectMapper HELPER_OBJECT_MAPPER = new ObjectMapper();

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private CoreConfigReloadCache coreConfigReloadCache;
    @Mock
    private EntitySyncStateStatusResolver syncStateStatusResolver;

    @InjectMocks
    private EntitySyncStateResolver syncStateResolver;

    @Test
    void resolve_cacheIsEmpty_returnsUnknownState() {
        // given
        when(coreConfigReloadCache.get()).thenReturn(null);

        // when
        EntitySyncState actualResult = syncStateResolver.resolve(new CurrentState(), true, 1, "entities", "entityName");

        // then
        assertThat(actualResult).isEqualTo(EntitySyncState.unknown());
    }

    @Test
    void resolve_configInCacheIsMissing_returnsUnknownState() {
        // given
        CoreConfigReloadCache.Entry cacheEntry = new CoreConfigReloadCache.Entry(null, 1);
        when(coreConfigReloadCache.get()).thenReturn(cacheEntry);

        // when
        EntitySyncState actualResult = syncStateResolver.resolve(new CurrentState(), true, 1, "entities", "entityName");

        // then
        assertThat(actualResult).isEqualTo(EntitySyncState.unknown());
    }

    @Test
    void resolve_configDoesNotHaveRequestedEntities_returnsUnknownState() throws JsonProcessingException {
        // given
        String configJson = "{}";
        JsonNode config = HELPER_OBJECT_MAPPER.readTree(configJson);
        CoreConfigReloadCache.Entry cacheEntry = new CoreConfigReloadCache.Entry(config, 1);
        when(coreConfigReloadCache.get()).thenReturn(cacheEntry);

        // when
        EntitySyncState actualResult = syncStateResolver.resolve(new CurrentState(), true, 1, "entities", "entityName");

        // then
        assertThat(actualResult).isEqualTo(EntitySyncState.unknown());
    }

    @Test
    void resolve_configHasRequestedEntitiesButDoesNotHaveEntityWithRequestedKey_returnsStateWithoutConfigStatePart() throws JsonProcessingException {
        // given
        long currentStateUpdatedAt = 1;
        long configReloadTimestamp = 2;

        CurrentState currentState = new CurrentState();
        ObjectNode currentStateJsonNode = JsonNodeFactory.instance.objectNode();

        String configJson = """
                {
                    "entities": {
                        "entityName_NEW": {
                            "prop1": "prop1Value",
                            "prop2": "prop2Value"
                        }
                    }
                }
                """;
        JsonNode config = HELPER_OBJECT_MAPPER.readTree(configJson);

        CoreConfigReloadCache.Entry cacheEntry = new CoreConfigReloadCache.Entry(config, configReloadTimestamp);
        when(coreConfigReloadCache.get()).thenReturn(cacheEntry);

        when(objectMapper.valueToTree(currentState)).thenReturn(currentStateJsonNode);

        when(syncStateStatusResolver.resolve(currentStateJsonNode, null, true, currentStateUpdatedAt, configReloadTimestamp))
                .thenReturn(EntitySyncStateStatus.IN_PROGRESS);

        EntitySyncState expectedResult = new EntitySyncState(currentStateJsonNode, null, EntitySyncStateStatus.IN_PROGRESS);

        // when
        EntitySyncState actualResult = syncStateResolver.resolve(currentState, true, currentStateUpdatedAt, "entities", "entityName");

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void resolve_configHasRequestedEntitiesAndHasEntityWithRequestedKey_returnsFullState() throws JsonProcessingException {
        // given
        long currentStateUpdatedAt = 1;
        long configReloadTimestamp = 2;

        CurrentState currentState = new CurrentState();
        ObjectNode currentStateJsonNode = JsonNodeFactory.instance.objectNode();

        String requestedEntityJson = """
                {
                    "prop1": "prop1Value",
                    "prop2": "prop2Value"
                }
                """;
        JsonNode requestedEntity = HELPER_OBJECT_MAPPER.readTree(requestedEntityJson);

        String configJson = """
                {
                    "entities": {
                        "entityName": {
                            "prop1": "prop1Value",
                            "prop2": "prop2Value"
                        }
                    }
                }
                """;
        JsonNode config = HELPER_OBJECT_MAPPER.readTree(configJson);

        CoreConfigReloadCache.Entry cacheEntry = new CoreConfigReloadCache.Entry(config, configReloadTimestamp);
        when(coreConfigReloadCache.get()).thenReturn(cacheEntry);

        when(objectMapper.valueToTree(currentState)).thenReturn(currentStateJsonNode);

        when(syncStateStatusResolver.resolve(currentStateJsonNode, requestedEntity, true, currentStateUpdatedAt, configReloadTimestamp))
                .thenReturn(EntitySyncStateStatus.FULLY_SYNCED);

        EntitySyncState expectedResult = new EntitySyncState(currentStateJsonNode, requestedEntity, EntitySyncStateStatus.FULLY_SYNCED);

        // when
        EntitySyncState actualResult = syncStateResolver.resolve(currentState, true, currentStateUpdatedAt, "entities", "entityName");

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void resolve_configHasRequestedEntitiesAndHasEntityWithRequestedKeyAndCurrentStateIsInvalid_returnsStateWithoutCurrentStatePart() throws JsonProcessingException {
        // given
        long currentStateUpdatedAt = 1;
        long configReloadTimestamp = 2;

        CurrentState currentState = new CurrentState();
        ObjectNode currentStateJsonNode = JsonNodeFactory.instance.objectNode();

        String requestedEntityJson = """
                {
                    "prop1": "prop1Value",
                    "prop2": "prop2Value"
                }
                """;
        JsonNode requestedEntity = HELPER_OBJECT_MAPPER.readTree(requestedEntityJson);

        String configJson = """
                {
                    "entities": {
                        "entityName": {
                            "prop1": "prop1Value",
                            "prop2": "prop2Value"
                        }
                    }
                }
                """;
        JsonNode config = HELPER_OBJECT_MAPPER.readTree(configJson);

        CoreConfigReloadCache.Entry cacheEntry = new CoreConfigReloadCache.Entry(config, configReloadTimestamp);
        when(coreConfigReloadCache.get()).thenReturn(cacheEntry);

        when(objectMapper.valueToTree(currentState)).thenReturn(currentStateJsonNode);

        when(syncStateStatusResolver.resolve(currentStateJsonNode, requestedEntity, false, currentStateUpdatedAt, configReloadTimestamp))
                .thenReturn(EntitySyncStateStatus.IN_PROGRESS);

        EntitySyncState expectedResult = new EntitySyncState(null, requestedEntity, EntitySyncStateStatus.IN_PROGRESS);

        // when
        EntitySyncState actualResult = syncStateResolver.resolve(currentState, false, currentStateUpdatedAt, "entities", "entityName");

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    private record CurrentState() {
    }
}