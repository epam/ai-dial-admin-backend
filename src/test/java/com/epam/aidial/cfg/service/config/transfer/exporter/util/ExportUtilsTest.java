package com.epam.aidial.cfg.service.config.transfer.exporter.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class ExportUtilsTest {

    @ParameterizedTest
    @MethodSource("hasAnyRequestedTopicTestParams")
    void testHasAnyRequestedTopic(Set<String> entityTopics, Set<String> requestedTopics, boolean expected) {
        assertThat(ExportUtils.hasAnyRequestedTopic(entityTopics, requestedTopics)).isEqualTo(expected);
    }

    private static Stream<Arguments> hasAnyRequestedTopicTestParams() {
        return Stream.of(
                // empty requested topics
                Arguments.of(Set.of("a", "b"), null, true),
                Arguments.of(Set.of("a", "b"), Set.of(), true),
                Arguments.of(null, null, true),
                Arguments.of(null, Set.of(), true),
                Arguments.of(Set.of(), null, true),
                Arguments.of(Set.of(), Set.of(), true),

                // not empty requested topics
                Arguments.of(null, Set.of("c", "d"), false),
                Arguments.of(Set.of(), Set.of("c", "d"), false),
                Arguments.of(Set.of("a", "b"), Set.of("c", "d"), false),
                Arguments.of(Set.of("a", "b", "c"), Set.of("c", "d"), true),
                Arguments.of(Set.of("a", "b", "c"), Set.of("b", "c", "d"), true)
        );
    }
}