package com.epam.aidial.cfg.domain.mapper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.stream.Stream;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TimeMapperImpl.class})
class TimeMapperTest {

    @Autowired
    private TimeMapper mapper;

    private static final long MS_IN_HOUR = 3600000L; // 1 hour in milliseconds

    // Tests for msToHours() method
    @ParameterizedTest
    @MethodSource("msToHoursTestData")
    void testMsToHours(Long milliseconds, Double expectedHours) {
        // when
        Double result = mapper.msToHours(milliseconds);

        // then
        Assertions.assertThat(result).isEqualTo(expectedHours);
    }

    static Stream<Arguments> msToHoursTestData() {
        return Stream.of(
                Arguments.of(3600000L, 1.0),        // 1 hour
                Arguments.of(null, null),           // null input
                Arguments.of(0L, 0.0),              // zero input
                Arguments.of(86400000L, 24.0),      // 24 hours
                Arguments.of(1800000L, 0.5)         // 0.5 hours
        );
    }

    // Tests for msToHoursWithTruncation() method
    @ParameterizedTest
    @MethodSource("msToHoursWithTruncationTestData")
    void testMsToHoursWithTruncation(Long milliseconds, Long expectedHours) {
        // when
        Long result = mapper.msToHoursWithTruncation(milliseconds);

        // then
        Assertions.assertThat(result).isEqualTo(expectedHours);
    }

    static Stream<Arguments> msToHoursWithTruncationTestData() {
        return Stream.of(
                Arguments.of(3600000L, 1L),         // 1 hour
                Arguments.of(null, null),           // null input
                Arguments.of(5400000L, 1L),         // 1.5 hours -> truncated to 1
                Arguments.of(0L, 0L),               // zero input
                Arguments.of(7199000L, 1L)          // 1.9997... hours -> truncated to 1
        );
    }

    // Tests for hoursToMs() method
    @ParameterizedTest
    @MethodSource("hoursToMsTestData")
    void testHoursToMs(Long hours, Long expectedMilliseconds) {
        // when
        Long result = mapper.hoursToMs(hours);

        // then
        Assertions.assertThat(result).isEqualTo(expectedMilliseconds);
    }

    static Stream<Arguments> hoursToMsTestData() {
        return Stream.of(
                Arguments.of(1L, 3600000L),         // 1 hour
                Arguments.of(null, null),           // null input
                Arguments.of(0L, 0L),               // zero input
                Arguments.of(24L, 86400000L)        // 24 hours
        );
    }
}

