package com.epam.aidial.cfg.domain.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSettingsValidator {

    private final CoreConfigVersionValidator coreConfigVersionValidator;

    public void validateCoreConfigVersionUpdate(String coreConfigVersion) {
        coreConfigVersionValidator.validateVersionFormat(coreConfigVersion);
    }
}
