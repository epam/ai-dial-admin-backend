package com.epam.aidial.cfg.domain.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class AdminSettingsValidatorTest {

    @Mock
    private CoreConfigVersionValidator coreConfigVersionValidator;

    @InjectMocks
    private AdminSettingsValidator adminSettingsValidator;

    @Test
    void validateCoreConfigVersionUpdate_shouldDelegateToCoreConfigVersionValidator() {
        // given
        String coreConfigVersion = "1.0.0";

        // when
        adminSettingsValidator.validateCoreConfigVersionUpdate(coreConfigVersion);

        // then
        verify(coreConfigVersionValidator).validateVersionFormat(coreConfigVersion);
    }

    @ParameterizedTest
    @CsvSource(value = {"null", "''", "' '"}, nullValues = "null")
    void validateCoreConfigVersionUpdate_shouldDoNothingIfBlankVersion(String coreConfigVersion) {
        // when
        adminSettingsValidator.validateCoreConfigVersionUpdate(coreConfigVersion);

        // then
        verify(coreConfigVersionValidator, never()).validateVersionFormat(coreConfigVersion);
    }
}