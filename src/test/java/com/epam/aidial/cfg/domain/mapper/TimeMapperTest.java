package com.epam.aidial.cfg.domain.mapper;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.concurrent.TimeUnit;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {TimeMapperImpl.class})
class TimeMapperTest {

    @Autowired
    private TimeMapper mapper;

    private static final long MS_IN_HOUR = 3600000L; // 1 hour in milliseconds

    // Tests for msToHours() method
    @Test
    void testMsToHours_ValidInput() {
        // given
        Long milliseconds = 3600000L; // 1 hour in milliseconds

        // when
        Double result = mapper.msToHours(milliseconds);

        // then
        Assertions.assertThat(result).isEqualTo(1.0);
    }

    @Test
    void testMsToHours_NullInput() {
        // given
        Long milliseconds = null;

        // when
        Double result = mapper.msToHours(milliseconds);

        // then
        Assertions.assertThat(result).isNull();
    }

    @Test
    void testMsToHours_ZeroInput() {
        // given
        Long milliseconds = 0L;

        // when
        Double result = mapper.msToHours(milliseconds);

        // then
        Assertions.assertThat(result).isEqualTo(0.0);
    }

    @Test
    void testMsToHours_LargeValues() {
        // given
        Long milliseconds = 86400000L; // 24 hours in milliseconds

        // when
        Double result = mapper.msToHours(milliseconds);

        // then
        Assertions.assertThat(result).isEqualTo(24.0);
    }

    @Test
    void testMsToHours_FractionalHours() {
        // given
        Long milliseconds = 1800000L; // 0.5 hours in milliseconds

        // when
        Double result = mapper.msToHours(milliseconds);

        // then
        Assertions.assertThat(result).isEqualTo(0.5);
    }

    // Tests for msToHoursWithTruncation() method
    @Test
    void testMsToHoursWithTruncation_ValidInput() {
        // given
        Long milliseconds = 3600000L; // 1 hour in milliseconds

        // when
        Long result = mapper.msToHoursWithTruncation(milliseconds);

        // then
        Assertions.assertThat(result).isEqualTo(1L);
    }

    @Test
    void testMsToHoursWithTruncation_NullInput() {
        // given
        Long milliseconds = null;

        // when
        Long result = mapper.msToHoursWithTruncation(milliseconds);

        // then
        Assertions.assertThat(result).isNull();
    }

    @Test
    void testMsToHoursWithTruncation_FractionalHours() {
        // given
        Long milliseconds = 5400000L; // 1.5 hours in milliseconds

        // when
        Long result = mapper.msToHoursWithTruncation(milliseconds);

        // then
        Assertions.assertThat(result).isEqualTo(1L); // Should truncate to 1
    }

    @Test
    void testMsToHoursWithTruncation_ZeroInput() {
        // given
        Long milliseconds = 0L;

        // when
        Long result = mapper.msToHoursWithTruncation(milliseconds);

        // then
        Assertions.assertThat(result).isEqualTo(0L);
    }

    @Test
    void testMsToHoursWithTruncation_AlmostTwoHours() {
        // given
        Long milliseconds = 7199000L; // 1.9997... hours in milliseconds

        // when
        Long result = mapper.msToHoursWithTruncation(milliseconds);

        // then
        Assertions.assertThat(result).isEqualTo(1L); // Should truncate to 1
    }

    // Tests for hoursToMs() method
    @Test
    void testHoursToMs_ValidInput() {
        // given
        Long hours = 1L;

        // when
        Long result = mapper.hoursToMs(hours);

        // then
        Assertions.assertThat(result).isEqualTo(3600000L);
    }

    @Test
    void testHoursToMs_NullInput() {
        // given
        Long hours = null;

        // when
        Long result = mapper.hoursToMs(hours);

        // then
        Assertions.assertThat(result).isNull();
    }

    @Test
    void testHoursToMs_ZeroInput() {
        // given
        Long hours = 0L;

        // when
        Long result = mapper.hoursToMs(hours);

        // then
        Assertions.assertThat(result).isEqualTo(0L);
    }

    @Test
    void testHoursToMs_LargeValues() {
        // given
        Long hours = 24L;

        // when
        Long result = mapper.hoursToMs(hours);

        // then
        Assertions.assertThat(result).isEqualTo(86400000L);
    }

    // Bidirectional consistency tests
    @Test
    void testBidirectionalConsistency_HoursToMsToHours() {
        // given
        Long originalHours = 5L;

        // when
        Long milliseconds = mapper.hoursToMs(originalHours);
        Double convertedBackHours = mapper.msToHours(milliseconds);

        // then
        Assertions.assertThat(convertedBackHours).isEqualTo(originalHours.doubleValue());
    }

    @Test
    void testBidirectionalConsistency_MsToHoursToMs() {
        // given
        Long originalMs = MS_IN_HOUR * 3; // 3 hours in milliseconds

        // when
        Double hours = mapper.msToHours(originalMs);
        Long convertedBackMs = mapper.hoursToMs(hours.longValue());

        // then
        Assertions.assertThat(convertedBackMs).isEqualTo(originalMs);
    }

    @Test
    void testTruncationBehavior_CompareWithRegularConversion() {
        // given
        Long milliseconds = 5400000L; // 1.5 hours

        // when
        Double regularHours = mapper.msToHours(milliseconds);
        Long truncatedHours = mapper.msToHoursWithTruncation(milliseconds);

        // then
        Assertions.assertThat(regularHours).isEqualTo(1.5);
        Assertions.assertThat(truncatedHours).isEqualTo(1L);
    }

    @Test
    void testConstantConsistency() {
        // Test that our test constant matches the mapper's internal constant
        // given
        Long oneHourMs = 3600000L;

        // when
        Double result = mapper.msToHours(oneHourMs);

        // then
        Assertions.assertThat(result).isEqualTo(1.0);
    }
}
