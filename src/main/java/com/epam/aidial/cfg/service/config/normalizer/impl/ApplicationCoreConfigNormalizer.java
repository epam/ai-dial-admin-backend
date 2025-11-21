package com.epam.aidial.cfg.service.config.normalizer.impl;

import com.epam.aidial.cfg.service.config.normalizer.CoreConfigNormalizer;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreApplication;
import com.epam.aidial.core.config.validation.CustomApplicationConformToTypeSchemaValidationContext;
import com.epam.aidial.core.config.validation.CustomApplicationConformToTypeSchemaValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApplicationCoreConfigNormalizer implements CoreConfigNormalizer {

    private final CustomApplicationConformToTypeSchemaValidator customApplicationConformToTypeSchemaValidator;

    @Override
    public void normalize(Config config) {
        Map<String, CoreApplication> applications = config.getApplications();
        if (MapUtils.isEmpty(applications)) {
            return;
        }

        var validationContext = new CustomApplicationConformToTypeSchemaValidationContext(config);
        for (Map.Entry<String, CoreApplication> applicationEntry : new HashMap<>(applications).entrySet()) {
            String applicationName = applicationEntry.getKey();
            CoreApplication application = applicationEntry.getValue();
            if (!customApplicationConformToTypeSchemaValidator.isValid(application, validationContext)) {
                log.debug("normalizeConfig. remove invalid application which doesn't conform to its schema: {}", applicationName);
                applications.remove(applicationName);
            }
        }
    }
}