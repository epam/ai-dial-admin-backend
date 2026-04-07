package com.epam.aidial.cfg.service.config.syncstate;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.model.EntitySyncState;
import com.epam.aidial.cfg.model.EntitySyncStateStatus;
import com.epam.aidial.cfg.service.config.reload.CoreConfigReloadCache;
import com.epam.aidial.cfg.service.config.transfer.VersionAwareFieldFilter;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntitySyncStateResolverTest {

    private static final ObjectMapper HELPER_OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();

    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ObjectMapper coreObjectMapper;
    @Mock
    private CoreConfigReloadCache coreConfigReloadCache;
    @Mock
    private EntitySyncStateStatusResolver syncStateStatusResolver;
    @Mock
    private VersionAwareFieldFilter versionAwareFieldFilter;

    private EntitySyncStateResolver syncStateResolver;

    @BeforeEach
    void setUp() {
        syncStateResolver = new EntitySyncStateResolver(
                objectMapper, coreObjectMapper, coreConfigReloadCache, syncStateStatusResolver, versionAwareFieldFilter);
    }

    @Test
    void resolveForEntityInObject_cacheIsEmpty_returnsUnknownState() {
        // given
        when(coreConfigReloadCache.get()).thenReturn(null);

        // when
        EntitySyncState actualResult = syncStateResolver.resolveForEntityInObject(new CurrentState(), true, 1, "entities", "entityName");

        // then
        assertThat(actualResult).isEqualTo(EntitySyncState.unknown());
    }

    @Test
    void resolveForEntityInObject_configInCacheIsMissing_returnsUnknownState() {
        // given
        CoreConfigReloadCache.Entry cacheEntry = new CoreConfigReloadCache.Entry(null, 1);
        when(coreConfigReloadCache.get()).thenReturn(cacheEntry);

        // when
        EntitySyncState actualResult = syncStateResolver.resolveForEntityInObject(new CurrentState(), true, 1, "entities", "entityName");

        // then
        assertThat(actualResult).isEqualTo(EntitySyncState.unknown());
    }

    @Test
    void resolveForEntityInObject_configDoesNotHaveRequestedEntities_returnsUnknownState() throws JsonProcessingException {
        // given
        String configJson = "{}";
        JsonNode config = HELPER_OBJECT_MAPPER.readTree(configJson);
        CoreConfigReloadCache.Entry cacheEntry = new CoreConfigReloadCache.Entry(config, 1);
        when(coreConfigReloadCache.get()).thenReturn(cacheEntry);

        // when
        EntitySyncState actualResult = syncStateResolver.resolveForEntityInObject(new CurrentState(), true, 1, "entities", "entityName");

        // then
        assertThat(actualResult).isEqualTo(EntitySyncState.unknown());
    }

    @Test
    void resolveForEntityInObject_configHasRequestedEntitiesButDoesNotHaveEntityWithRequestedKey_returnsStateWithoutConfigStatePart() throws JsonProcessingException {
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

        String currentStateJsonNodeAsString = "currentStateJsonNodeAsString";
        when(objectMapper.writeValueAsString(currentState)).thenReturn(currentStateJsonNodeAsString);
        when(coreObjectMapper.readValue(currentStateJsonNodeAsString, CurrentState.class)).thenReturn(currentState);
        when(coreObjectMapper.writeValueAsString(currentState)).thenReturn(currentStateJsonNodeAsString);
        when(objectMapper.readTree(currentStateJsonNodeAsString)).thenReturn(currentStateJsonNode);

        when(versionAwareFieldFilter.filterEntityNodeForTargetVersion(currentStateJsonNode, "entities")).thenReturn(currentStateJsonNode);

        when(syncStateStatusResolver.resolve(currentStateJsonNode, null, CurrentState.class, true, currentStateUpdatedAt, configReloadTimestamp))
                .thenReturn(EntitySyncStateStatus.IN_PROGRESS);

        EntitySyncState expectedResult = new EntitySyncState(currentStateJsonNode, null, EntitySyncStateStatus.IN_PROGRESS);

        // when
        EntitySyncState actualResult = syncStateResolver.resolveForEntityInObject(currentState, true, currentStateUpdatedAt, "entities", "entityName");

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void resolveForEntityInObject_configHasRequestedEntitiesAndHasEntityWithRequestedKey_returnsFullState() throws JsonProcessingException {
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

        String currentStateJsonNodeAsString = "currentStateJsonNodeAsString";
        when(objectMapper.writeValueAsString(currentState)).thenReturn(currentStateJsonNodeAsString);
        when(coreObjectMapper.readValue(currentStateJsonNodeAsString, CurrentState.class)).thenReturn(currentState);
        when(coreObjectMapper.writeValueAsString(currentState)).thenReturn(currentStateJsonNodeAsString);
        when(objectMapper.readTree(currentStateJsonNodeAsString)).thenReturn(currentStateJsonNode);

        when(versionAwareFieldFilter.filterEntityNodeForTargetVersion(currentStateJsonNode, "entities")).thenReturn(currentStateJsonNode);

        when(syncStateStatusResolver.resolve(currentStateJsonNode, requestedEntity, CurrentState.class, true, currentStateUpdatedAt, configReloadTimestamp))
                .thenReturn(EntitySyncStateStatus.FULLY_SYNCED);

        EntitySyncState expectedResult = new EntitySyncState(currentStateJsonNode, requestedEntity, EntitySyncStateStatus.FULLY_SYNCED);

        // when
        EntitySyncState actualResult = syncStateResolver.resolveForEntityInObject(currentState, true, currentStateUpdatedAt, "entities", "entityName");

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void resolveForEntityInObject_configHasRequestedEntitiesAndHasEntityWithRequestedKeyAndCurrentStateIsInvalid_returnsStateWithoutCurrentStatePart()
            throws JsonProcessingException {
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

        String currentStateJsonNodeAsString = "currentStateJsonNodeAsString";
        when(objectMapper.writeValueAsString(currentState)).thenReturn(currentStateJsonNodeAsString);
        when(coreObjectMapper.readValue(currentStateJsonNodeAsString, CurrentState.class)).thenReturn(currentState);
        when(coreObjectMapper.writeValueAsString(currentState)).thenReturn(currentStateJsonNodeAsString);
        when(objectMapper.readTree(currentStateJsonNodeAsString)).thenReturn(currentStateJsonNode);

        when(versionAwareFieldFilter.filterEntityNodeForTargetVersion(currentStateJsonNode, "entities")).thenReturn(currentStateJsonNode);

        when(syncStateStatusResolver.resolve(currentStateJsonNode, requestedEntity, CurrentState.class, false, currentStateUpdatedAt, configReloadTimestamp))
                .thenReturn(EntitySyncStateStatus.IN_PROGRESS);

        EntitySyncState expectedResult = new EntitySyncState(null, requestedEntity, EntitySyncStateStatus.IN_PROGRESS);

        // when
        EntitySyncState actualResult = syncStateResolver.resolveForEntityInObject(currentState, false, currentStateUpdatedAt, "entities", "entityName");

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void resolveForEntityInArray_cacheIsEmpty_returnsUnknownState() {
        // given
        when(coreConfigReloadCache.get()).thenReturn(null);

        // when
        EntitySyncState actualResult = syncStateResolver.resolveForEntityInArray(new CurrentState(), true, 1, "entities", "name", "entityName");

        // then
        assertThat(actualResult).isEqualTo(EntitySyncState.unknown());
    }

    @Test
    void resolveForEntityInArray_configInCacheIsMissing_returnsUnknownState() {
        // given
        CoreConfigReloadCache.Entry cacheEntry = new CoreConfigReloadCache.Entry(null, 1);
        when(coreConfigReloadCache.get()).thenReturn(cacheEntry);

        // when
        EntitySyncState actualResult = syncStateResolver.resolveForEntityInArray(new CurrentState(), true, 1, "entities", "name", "entityName");

        // then
        assertThat(actualResult).isEqualTo(EntitySyncState.unknown());
    }

    @Test
    void resolveForEntityInArray_configDoesNotHaveRequestedEntities_returnsUnknownState() throws JsonProcessingException {
        // given
        String configJson = "{}";
        JsonNode config = HELPER_OBJECT_MAPPER.readTree(configJson);
        CoreConfigReloadCache.Entry cacheEntry = new CoreConfigReloadCache.Entry(config, 1);
        when(coreConfigReloadCache.get()).thenReturn(cacheEntry);

        // when
        EntitySyncState actualResult = syncStateResolver.resolveForEntityInArray(new CurrentState(), true, 1, "entities", "name", "entityName");

        // then
        assertThat(actualResult).isEqualTo(EntitySyncState.unknown());
    }

    @Test
    void resolveForEntityInArray_configHasRequestedEntitiesButDoesNotHaveEntityWithRequestedKey_returnsStateWithoutConfigStatePart() throws JsonProcessingException {
        // given
        long currentStateUpdatedAt = 1;
        long configReloadTimestamp = 2;

        CurrentState currentState = new CurrentState();
        ObjectNode currentStateJsonNode = JsonNodeFactory.instance.objectNode();

        String configJson = """
                {
                    "entities": [
                        {
                            "name": "entityName_NEW",
                            "prop1": "prop1Value",
                            "prop2": "prop2Value"
                        }
                    ]
                }
                """;
        JsonNode config = HELPER_OBJECT_MAPPER.readTree(configJson);

        CoreConfigReloadCache.Entry cacheEntry = new CoreConfigReloadCache.Entry(config, configReloadTimestamp);
        when(coreConfigReloadCache.get()).thenReturn(cacheEntry);

        String currentStateJsonNodeAsString = "currentStateJsonNodeAsString";
        when(objectMapper.writeValueAsString(currentState)).thenReturn(currentStateJsonNodeAsString);
        when(coreObjectMapper.readValue(currentStateJsonNodeAsString, CurrentState.class)).thenReturn(currentState);
        when(coreObjectMapper.writeValueAsString(currentState)).thenReturn(currentStateJsonNodeAsString);
        when(objectMapper.readTree(currentStateJsonNodeAsString)).thenReturn(currentStateJsonNode);

        when(versionAwareFieldFilter.filterEntityNodeForTargetVersion(currentStateJsonNode, "entities")).thenReturn(currentStateJsonNode);

        when(syncStateStatusResolver.resolve(currentStateJsonNode, null, CurrentState.class, true, currentStateUpdatedAt, configReloadTimestamp))
                .thenReturn(EntitySyncStateStatus.IN_PROGRESS);

        EntitySyncState expectedResult = new EntitySyncState(currentStateJsonNode, null, EntitySyncStateStatus.IN_PROGRESS);

        // when
        EntitySyncState actualResult = syncStateResolver.resolveForEntityInArray(currentState, true, currentStateUpdatedAt, "entities", "name", "entityName");

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void resolveForEntityInArray_configHasRequestedEntitiesAndHasEntityWithRequestedKey_returnsFullState() throws JsonProcessingException {
        // given
        long currentStateUpdatedAt = 1;
        long configReloadTimestamp = 2;

        CurrentState currentState = new CurrentState();
        ObjectNode currentStateJsonNode = JsonNodeFactory.instance.objectNode();

        String requestedEntityJson = """
                {
                    "name": "entityName",
                    "prop1": "prop1Value",
                    "prop2": "prop2Value"
                }
                """;
        JsonNode requestedEntity = HELPER_OBJECT_MAPPER.readTree(requestedEntityJson);

        String configJson = """
                {
                    "entities": [
                        {
                            "name": "entityName",
                            "prop1": "prop1Value",
                            "prop2": "prop2Value"
                        }
                    ]
                }
                """;
        JsonNode config = HELPER_OBJECT_MAPPER.readTree(configJson);

        CoreConfigReloadCache.Entry cacheEntry = new CoreConfigReloadCache.Entry(config, configReloadTimestamp);
        when(coreConfigReloadCache.get()).thenReturn(cacheEntry);

        String currentStateJsonNodeAsString = "currentStateJsonNodeAsString";
        when(objectMapper.writeValueAsString(currentState)).thenReturn(currentStateJsonNodeAsString);
        when(coreObjectMapper.readValue(currentStateJsonNodeAsString, CurrentState.class)).thenReturn(currentState);
        when(coreObjectMapper.writeValueAsString(currentState)).thenReturn(currentStateJsonNodeAsString);
        when(objectMapper.readTree(currentStateJsonNodeAsString)).thenReturn(currentStateJsonNode);

        when(versionAwareFieldFilter.filterEntityNodeForTargetVersion(currentStateJsonNode, "entities")).thenReturn(currentStateJsonNode);

        when(syncStateStatusResolver.resolve(currentStateJsonNode, requestedEntity, CurrentState.class, true, currentStateUpdatedAt, configReloadTimestamp))
                .thenReturn(EntitySyncStateStatus.FULLY_SYNCED);

        EntitySyncState expectedResult = new EntitySyncState(currentStateJsonNode, requestedEntity, EntitySyncStateStatus.FULLY_SYNCED);

        // when
        EntitySyncState actualResult = syncStateResolver.resolveForEntityInArray(currentState, true, currentStateUpdatedAt, "entities", "name", "entityName");

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    @Test
    void resolveForEntityInArray_configHasRequestedEntitiesAndHasEntityWithRequestedKeyAndCurrentStateIsInvalid_returnsStateWithoutCurrentStatePart()
            throws JsonProcessingException {
        // given
        long currentStateUpdatedAt = 1;
        long configReloadTimestamp = 2;

        CurrentState currentState = new CurrentState();
        ObjectNode currentStateJsonNode = JsonNodeFactory.instance.objectNode();

        String requestedEntityJson = """
                {
                    "name": "entityName",
                    "prop1": "prop1Value",
                    "prop2": "prop2Value"
                }
                """;
        JsonNode requestedEntity = HELPER_OBJECT_MAPPER.readTree(requestedEntityJson);

        String configJson = """
                {
                    "entities": [
                        {
                            "name": "entityName",
                            "prop1": "prop1Value",
                            "prop2": "prop2Value"
                        }
                    ]
                }
                """;
        JsonNode config = HELPER_OBJECT_MAPPER.readTree(configJson);

        CoreConfigReloadCache.Entry cacheEntry = new CoreConfigReloadCache.Entry(config, configReloadTimestamp);
        when(coreConfigReloadCache.get()).thenReturn(cacheEntry);

        String currentStateJsonNodeAsString = "currentStateJsonNodeAsString";
        when(objectMapper.writeValueAsString(currentState)).thenReturn(currentStateJsonNodeAsString);
        when(coreObjectMapper.readValue(currentStateJsonNodeAsString, CurrentState.class)).thenReturn(currentState);
        when(coreObjectMapper.writeValueAsString(currentState)).thenReturn(currentStateJsonNodeAsString);
        when(objectMapper.readTree(currentStateJsonNodeAsString)).thenReturn(currentStateJsonNode);

        when(versionAwareFieldFilter.filterEntityNodeForTargetVersion(currentStateJsonNode, "entities")).thenReturn(currentStateJsonNode);

        when(syncStateStatusResolver.resolve(currentStateJsonNode, requestedEntity, CurrentState.class, false, currentStateUpdatedAt, configReloadTimestamp))
                .thenReturn(EntitySyncStateStatus.IN_PROGRESS);

        EntitySyncState expectedResult = new EntitySyncState(null, requestedEntity, EntitySyncStateStatus.IN_PROGRESS);

        // when
        EntitySyncState actualResult = syncStateResolver.resolveForEntityInArray(currentState, false, currentStateUpdatedAt, "entities", "name", "entityName");

        // then
        assertThat(actualResult).isEqualTo(expectedResult);
    }

    private record CurrentState() {
    }
}