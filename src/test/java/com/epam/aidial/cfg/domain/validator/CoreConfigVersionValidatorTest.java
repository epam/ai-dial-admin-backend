package com.epam.aidial.cfg.domain.validator;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CoreConfigVersionValidatorTest {

    private CoreConfigVersionValidator coreConfigVersionValidator;

    @BeforeEach
    void setUp() {
        coreConfigVersionValidator = new CoreConfigVersionValidator();
    }

    @Test
    void validateVersionFormat_shouldDoNothingForLatestVersion() {
        Assertions.assertThatNoException().isThrownBy(() -> coreConfigVersionValidator.validateVersionFormat("latest"));
    }

    @Test
    void validateVersionFormat_shouldDoNothingForValidVersionFormat() {
        Assertions.assertThatNoException().isThrownBy(() -> coreConfigVersionValidator.validateVersionFormat("1.0.2"));
    }

    @Test
    void validateVersionFormat_shouldThrowExceptionForInvalidVersionFormat() {
        Assertions.assertThatThrownBy(() -> coreConfigVersionValidator.validateVersionFormat("1.0.2-rc"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid version format: 1.0.2-rc. Expected format: X.Y.Z (e.g., '0.23.0', '1.0.1', '2.0.3')) or 'latest'");
    }
}