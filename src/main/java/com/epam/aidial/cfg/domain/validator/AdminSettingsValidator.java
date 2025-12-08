package com.epam.aidial.cfg.domain.validator;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AdminSettingsValidator {

    private final CoreConfigVersionValidator coreConfigVersionValidator;

    public void validateCoreConfigVersionUpdate(String coreConfigVersion) {
        if (StringUtils.isNotBlank(coreConfigVersion)) {
            coreConfigVersionValidator.validateVersionFormat(coreConfigVersion);
        }
    }
}
