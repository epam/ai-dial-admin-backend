package com.epam.aidial.cfg.service.config.syncstate;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.model.EntitySyncStateStatus;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

class EntitySyncStateStatusResolverTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();

    private EntitySyncStateStatusResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new EntitySyncStateStatusResolver(10);
    }

    @ParameterizedTest
    @MethodSource("resolveTestParams")
    void resolve(JsonNode currentState,
                 JsonNode configState,
                 boolean isCurrentStateValid,
                 long currentStateUpdatedAt,
                 long configReloadTimestamp,
                 EntitySyncStateStatus expectedResult) {
        EntitySyncStateStatus actualResult = resolver.resolve(
                currentState,
                configState,
                isCurrentStateValid,
                currentStateUpdatedAt,
                configReloadTimestamp
        );
        Assertions.assertThat(actualResult).isEqualTo(expectedResult);
    }

    private static Stream<Arguments> resolveTestParams() throws JsonProcessingException {
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