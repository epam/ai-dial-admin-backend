package com.epam.aidial.cfg.domain.validator;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
}