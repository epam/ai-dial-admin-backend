package com.epam.aidial.cfg.service.config.syncstate;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.model.EntitySyncStateStatus;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.utils.json.JsonNodeComparator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EntitySyncStateStatusResolverTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();

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

    @ParameterizedTest
    @MethodSource("resolve_useJsonNodeEqualsAsComparatorTestParams")
    void resolve_useJsonNodeEqualsAsComparator(JsonNode currentState,
                                               JsonNode configState,
                                               boolean isCurrentStateValid,
                                               long currentStateUpdatedAt,
                                               long configReloadTimestamp,
                                               EntitySyncStateStatus expectedResult) {
        EntitySyncStateStatus actualResult = resolver.resolve(
                currentState,
                configState,
                String.class,
                isCurrentStateValid,
                currentStateUpdatedAt,
                configReloadTimestamp
        );
        Assertions.assertThat(actualResult).isEqualTo(expectedResult);
    }

    private static Stream<Arguments> resolve_useJsonNodeEqualsAsComparatorTestParams() throws JsonProcessingException {
        JsonNode currentState = currentState();
        JsonNode configStateWithDifferentFieldOrderAndSameFieldValues = configStateWithDifferentFieldOrderAndSameFieldValues();
        JsonNode configStateWithSameFieldOrderAndDifferentFieldValues = configStateWithSameFieldOrderAndDifferentFieldValues();
        JsonNode configStateWithSameFieldOrderAndSameFieldValuesAndDifferentArrayElementsOrder = configStateWithSameFieldOrderAndSameFieldValuesAndDifferentArrayElementsOrder();

        return Stream.of(
                Arguments.of(currentState, null, true, 1, 1, EntitySyncStateStatus.IN_PROGRESS),
                Arguments.of(currentState, null, true, 1, 12, EntitySyncStateStatus.IN_PROGRESS_TOO_LONG),

                Arguments.of(currentState, currentState,
                        true, 1, 1, EntitySyncStateStatus.FULLY_SYNCED),
                Arguments.of(currentState, configStateWithDifferentFieldOrderAndSameFieldValues,
                        true, 1, 1, EntitySyncStateStatus.FULLY_SYNCED),

                Arguments.of(currentState, configStateWithSameFieldOrderAndDifferentFieldValues,
                        true, 1, 1, EntitySyncStateStatus.IN_PROGRESS),
                Arguments.of(currentState, configStateWithSameFieldOrderAndSameFieldValuesAndDifferentArrayElementsOrder,
                        true, 1, 1, EntitySyncStateStatus.IN_PROGRESS),
                Arguments.of(currentState, configStateWithNewField(),
                        true, 1, 1, EntitySyncStateStatus.IN_PROGRESS),
                Arguments.of(currentState, configStateWithMissingField(),
                        true, 1, 1, EntitySyncStateStatus.IN_PROGRESS),

                Arguments.of(currentState, configStateWithSameFieldOrderAndDifferentFieldValues,
                        true, 1, 12, EntitySyncStateStatus.IN_PROGRESS_TOO_LONG),
                Arguments.of(currentState, configStateWithSameFieldOrderAndSameFieldValuesAndDifferentArrayElementsOrder,
                        true, 1, 12, EntitySyncStateStatus.IN_PROGRESS_TOO_LONG),
                Arguments.of(currentState, configStateWithNewField(),
                        true, 1, 12, EntitySyncStateStatus.IN_PROGRESS_TOO_LONG),
                Arguments.of(currentState, configStateWithMissingField(),
                        true, 1, 12, EntitySyncStateStatus.IN_PROGRESS_TOO_LONG),

                Arguments.of(currentState, null, false, 1, 1, EntitySyncStateStatus.FULLY_SYNCED),

                Arguments.of(currentState, configStateWithNewField(), false, 1, 1, EntitySyncStateStatus.IN_PROGRESS),

                Arguments.of(currentState, configStateWithNewField(), false, 1, 12, EntitySyncStateStatus.IN_PROGRESS_TOO_LONG)
        );
    }

    private static JsonNode currentState() throws JsonProcessingException {
        return OBJECT_MAPPER.readTree(ResourceUtils.readResource("/domain/resolver/entity-sync-state/status/current_state.json"));
    }

    private static JsonNode configStateWithSameFieldOrderAndDifferentFieldValues() throws JsonProcessingException {
        return OBJECT_MAPPER.readTree(ResourceUtils.readResource("/domain/resolver/entity-sync-state/status/config_state_with_same_field_order_and_different_field_values.json"));
    }

    private static JsonNode configStateWithDifferentFieldOrderAndSameFieldValues() throws JsonProcessingException {
        return OBJECT_MAPPER.readTree(ResourceUtils.readResource("/domain/resolver/entity-sync-state/status/config_state_with_different_field_order_and_same_field_values.json"));
    }

    private static JsonNode configStateWithSameFieldOrderAndSameFieldValuesAndDifferentArrayElementsOrder() throws JsonProcessingException {
        return OBJECT_MAPPER.readTree(ResourceUtils.readResource(
                "/domain/resolver/entity-sync-state/status/config_state_with_same_field_order_and_same_field_values_and_different_array_elements_order.json"
        ));
    }

    private static JsonNode configStateWithNewField() throws JsonProcessingException {
        return OBJECT_MAPPER.readTree(ResourceUtils.readResource("/domain/resolver/entity-sync-state/status/config_state_with_new_field.json"));
    }

    private static JsonNode configStateWithMissingField() throws JsonProcessingException {
        return OBJECT_MAPPER.readTree(ResourceUtils.readResource("/domain/resolver/entity-sync-state/status/config_state_with_missing_field.json"));
    }

}