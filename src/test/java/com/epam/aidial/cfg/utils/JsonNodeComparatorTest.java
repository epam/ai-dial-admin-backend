package com.epam.aidial.cfg.utils;

import com.epam.aidial.cfg.utils.json.JsonNodeComparator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

class JsonNodeComparatorTest {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonNodeComparator comparator;

    @BeforeEach
    void setUp() {
        comparator = new JsonNodeComparator(Set.of("root.*.alpha", "root.*.alpha.labels"));
    }


    @ParameterizedTest
    @MethodSource("equalsTestParams")
    void testEquals(JsonNode n1, JsonNode n2, boolean expectedResult) {
        boolean actualResult = comparator.equals(n1, n2);
        Assertions.assertThat(actualResult).isEqualTo(expectedResult);
    }


    private static Stream<Arguments> equalsTestParams() throws JsonProcessingException {
        JsonNode original = original();

        return Stream.of(
                Arguments.of(original, original, true),
                Arguments.of(original, originalWithDifferentArrayElementsOrder(), false),
                Arguments.of(original, originalWithDifferentArrayElementsOrderConfiguredAsIgnoreOrder(), true),
                Arguments.of(original, originalWithDifferentFieldValuesAndSameFieldOrder(), false),
                Arguments.of(original, originalWithExtraField(), false),
                Arguments.of(original, originalWithMissingField(), false),
                Arguments.of(original, originalWithSameFieldValuesAndDifferentFieldOrder(), true)
        );
    }

    private static JsonNode original() throws JsonProcessingException {
        return readTree("/utils/json_comparator/original.json");
    }

    private static JsonNode originalWithDifferentArrayElementsOrder() throws JsonProcessingException {
        return readTree("/utils/json_comparator/original_with_different_array_elements_order.json");
    }

    private static JsonNode originalWithDifferentArrayElementsOrderConfiguredAsIgnoreOrder() throws JsonProcessingException {
        return readTree("/utils/json_comparator/original_with_different_array_elements_order_configured_as_ignore_order.json");
    }

    private static JsonNode originalWithDifferentFieldValuesAndSameFieldOrder() throws JsonProcessingException {
        return readTree("/utils/json_comparator/original_with_different_field_values_and_same_field_order.json");
    }

    private static JsonNode originalWithExtraField() throws JsonProcessingException {
        return readTree("/utils/json_comparator/original_with_extra_field.json");
    }

    private static JsonNode originalWithMissingField() throws JsonProcessingException {
        return readTree("/utils/json_comparator/original_with_missing_field.json");
    }

    private static JsonNode originalWithSameFieldValuesAndDifferentFieldOrder() throws JsonProcessingException {
        return readTree("/utils/json_comparator/original_with_same_field_values_and_different_field_order.json");
    }

    private static JsonNode readTree(String path) throws JsonProcessingException {
        return MAPPER.readTree(ResourceUtils.readResource(path));
    }
}