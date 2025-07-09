package com.epam.aidial.cfg.dao.validator;

import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.validator.InterceptorValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InterceptorValidatorTest {

    private static final String NAME_VALIDATION_PATTERN = "^[a-zA-Z0-9-_.]{1,30}$";

    private InterceptorValidator interceptorValidator;

    @BeforeEach
    void setUp() {
        interceptorValidator = new InterceptorValidator();
    }

    @Test
    void validateUpdate_shouldThrowExceptionWhenInterceptorNameIsUpdated() {
        Interceptor interceptor = new Interceptor();
        interceptor.setName("new_interceptor_name");
        interceptor.setEndpoint("https://test.endpoint.com");

        assertThatThrownBy(() -> interceptorValidator.validateUpdate("interceptor_name", interceptor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Interceptor with name: 'interceptor_name' can not be renamed. New interceptor name: 'new_interceptor_name'");
    }

    @Test
    void validateUpdate_shouldDoNothingWhenInterceptorNameIsNotUpdated() {
        Interceptor interceptor = new Interceptor();
        interceptor.setName("interceptor_name");
        interceptor.setEndpoint("https://test.endpoint.com");

        assertThatNoException().isThrownBy(() -> interceptorValidator.validateUpdate("interceptor_name", interceptor));
    }

    @ParameterizedTest
    @ValueSource(strings = {"valid-name", "valid_name", "ValidName123", "name-123_456", "name.with.dots"})
    void validateCreation_shouldNotThrowExceptionForValidName(String name) {
        // given
        ReflectionTestUtils.setField(interceptorValidator, "interceptorNameValidationPattern", NAME_VALIDATION_PATTERN);

        Interceptor interceptor = new Interceptor();
        interceptor.setName(name);
        interceptor.setEndpoint("https://test.endpoint.com");

        // when/then
        assertThatNoException().isThrownBy(() -> interceptorValidator.validateCreation(interceptor));
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid name with spaces", "invalid@name", "invalid#name", "invalid$name", 
            "name-that-is-way-too-long-for-validation-pattern"})
    void validateCreation_shouldThrowExceptionForInvalidName(String name) {
        // given
        ReflectionTestUtils.setField(interceptorValidator, "interceptorNameValidationPattern", NAME_VALIDATION_PATTERN);

        Interceptor interceptor = new Interceptor();
        interceptor.setName(name);
        interceptor.setEndpoint("https://test.endpoint.com");

        // when/then
        assertThatThrownBy(() -> interceptorValidator.validateCreation(interceptor))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("does not match the required pattern");
    }

}