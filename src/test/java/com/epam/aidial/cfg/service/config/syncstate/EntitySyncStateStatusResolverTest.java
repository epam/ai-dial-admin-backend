package com.epam.aidial.cfg.service.config.syncstate;

import com.epam.aidial.cfg.model.EntitySyncStateStatus;
import com.epam.aidial.cfg.utils.json.JsonNodeComparator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntitySyncStateStatusResolverTest {

    @Mock
    private JsonNodeComparator jsonNodeComparator;

    private EntitySyncStateStatusResolver resolver;

    private JsonNode currentState;
    private JsonNode configState;

    @BeforeEach
    void setUp() {
        resolver = new EntitySyncStateStatusResolver(Map.of(Object.class, jsonNodeComparator), 10);

        currentState = JsonNodeFactory.instance.objectNode();
        configState = JsonNodeFactory.instance.objectNode();
    }

    @Test
    void resolve_currentStateIsValidAndStatesAreEqual_returnsFullySynced() {
        // given
        when(jsonNodeComparator.equals(currentState, configState)).thenReturn(true);

        // when
        EntitySyncStateStatus result = resolver.resolve(
                currentState,
                configState,
                Object.class,
                true,
                1,
                1
        );

        // then
        assertThat(result).isEqualTo(EntitySyncStateStatus.FULLY_SYNCED);
    }

    @Test
    void resolve_currentStateIsInvalidAndConfigStateIsNull_returnsFullySynced() {
        // when
        EntitySyncStateStatus result = resolver.resolve(
                currentState,
                null,
                Object.class,
                false,
                1,
                1
        );

        // then
        assertThat(result).isEqualTo(EntitySyncStateStatus.FULLY_SYNCED);
        verifyNoInteractions(jsonNodeComparator);
    }

    @Test
    void resolve_currentStateIsValidAndStatesAreNotEqualAndThresholdNotExceeded_returnsInProgress() {
        // given
        when(jsonNodeComparator.equals(currentState, configState)).thenReturn(false);

        // when
        EntitySyncStateStatus result = resolver.resolve(
                currentState,
                configState,
                Object.class,
                true,
                1,
                1
        );

        // then
        assertThat(result).isEqualTo(EntitySyncStateStatus.IN_PROGRESS);
    }

    @Test
    void resolve_currentStateIsInvalidAndConfigStateIsNotNullAndThresholdNotExceeded_returnsInProgress() {
        // when
        EntitySyncStateStatus result = resolver.resolve(
                currentState,
                configState,
                Object.class,
                false,
                1,
                1
        );

        // then
        assertThat(result).isEqualTo(EntitySyncStateStatus.IN_PROGRESS);
        verifyNoInteractions(jsonNodeComparator);
    }

    @Test
    void resolve_currentStateIsValidAndStatesAreNotEqualAndThresholdExceeded_returnInProgressTooLong() {
        // given
        when(jsonNodeComparator.equals(currentState, configState)).thenReturn(false);

        // when
        EntitySyncStateStatus result = resolver.resolve(
                currentState,
                configState,
                Object.class,
                true,
                1,
                12
        );

        // then
        assertThat(result).isEqualTo(EntitySyncStateStatus.IN_PROGRESS_TOO_LONG);
    }

    @Test
    void resolve_currentStateIsInvalidAndConfigStateIsNotNullAndThresholdExceeded_returnsInProgress() {
        // when
        EntitySyncStateStatus result = resolver.resolve(
                currentState,
                configState,
                Object.class,
                false,
                1,
                12
        );

        // then
        assertThat(result).isEqualTo(EntitySyncStateStatus.IN_PROGRESS_TOO_LONG);
        verifyNoInteractions(jsonNodeComparator);
    }

}