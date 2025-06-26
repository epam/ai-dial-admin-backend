package com.epam.aidial.cfg.dao.validator;

import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.validator.InterceptorValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InterceptorValidatorTest {

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

}