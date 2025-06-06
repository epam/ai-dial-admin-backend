package com.epam.aidial.cfg.utils;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class SecretUtilsTest {

    @Test
    void testMask_Null_Null() {
        // given
        // when
        String masked = SecretUtils.mask(null);
        // then
        Assertions.assertThat(masked).isNull();
    }

    @ParameterizedTest
    @ValueSource(strings = {"q1w2e3r4t5", "a", "bc"})
    void testMask_LengthLessFifteen(String value) {
        // given
        String expected = "**********";
        // when
        String masked = SecretUtils.mask(value);
        // then
        Assertions.assertThat(masked).isNotNull()
                .isEqualTo(expected);
    }

    @Test
    void testMask_LengthMoreFifteen() {
        // given
        String value = "q1w2e3r4t5y6u7i8";
        String expected = "q1w*******";
        // when
        String masked = SecretUtils.mask(value);
        // then
        Assertions.assertThat(masked).isNotNull()
                .isEqualTo(expected);
    }

}