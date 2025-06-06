package com.epam.aidial.cfg.dto.validation.validator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class HttpStatusValidatorTest {

    private HttpStatusValidator validator;

    @BeforeEach
    void init() {
        validator = new HttpStatusValidator();
    }

    @ParameterizedTest
    @ValueSource(ints = {200, 400, 500})
    void testIsValid_Success(int status) {
        var result = validator.isValid(status, null);
        Assertions.assertThat(result).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 10, 99})
    void testIsValid_IsNotValid(int status) {
        var result = validator.isValid(status, null);
        Assertions.assertThat(result).isFalse();
    }

}