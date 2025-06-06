package com.epam.aidial.cfg.dto.validation.validator;

import com.epam.aidial.cfg.dto.LimitDto;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

class RoleLimitsValidatorTest {

    private RoleLimitsValidator validator;

    @BeforeEach
    void init() {
        validator = new RoleLimitsValidator();
    }

    @ParameterizedTest
    @MethodSource("validRoleLimits")
    void testIsValid_Success(Map<String, LimitDto> roleLimits) {
        var result = validator.isValid(roleLimits, null);
        Assertions.assertThat(result).isTrue();
    }

    @Test
    void testIsValid_IsNotValid() {
        var limitDto = new LimitDto();
        var roleLimits = Map.of("default", limitDto);
        var result = validator.isValid(roleLimits, null);
        Assertions.assertThat(result).isFalse();
    }

    private static Stream<Arguments> validRoleLimits() {
        var limitDto = new LimitDto();
        return Stream.of(
                Arguments.of(Map.of()),
                Arguments.of(Map.of("test", limitDto))
        );
    }

}