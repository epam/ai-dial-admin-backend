package com.epam.aidial.cfg.service.normalizer.impl;

import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreApplication;
import com.epam.aidial.core.config.validation.CustomApplicationConformToTypeSchemaValidationContext;
import com.epam.aidial.core.config.validation.CustomApplicationConformToTypeSchemaValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApplicationCoreConfigNormalizerTest {

    @Mock
    private CustomApplicationConformToTypeSchemaValidator customApplicationConformToTypeSchemaValidator;

    @InjectMocks
    private ApplicationCoreConfigNormalizer normalizer;

    @Test
    void testNormalize_applicationsMissingInConfig_doNothing() {
        // given
        Config config = new Config();

        // when
        normalizer.normalize(config);

        // then
        assertThat(config.getApplications()).isEmpty();
        verifyNoInteractions(customApplicationConformToTypeSchemaValidator);
    }

    @Test
    void testNormalize_applicationDoesNotConformToItsSchema_RemoveApplication() {
        // given
        CoreApplication application = new CoreApplication();

        Map<String, CoreApplication> applicationsMap = new HashMap<>();
        applicationsMap.put("testApplication", application);

        Config config = new Config();
        config.setApplications(applicationsMap);

        when(customApplicationConformToTypeSchemaValidator.isValid(
                eq(application),
                any(CustomApplicationConformToTypeSchemaValidationContext.class))
        ).thenReturn(false);

        // when
        normalizer.normalize(config);

        // then
        assertThat(config.getApplications()).isEmpty();
    }

    @Test
    void testNormalize_applicationConformsToItsSchema_DoesNotRemoveApplication() {
        // given
        CoreApplication application = new CoreApplication();

        Map<String, CoreApplication> applicationsMap = new HashMap<>();
        applicationsMap.put("testApplication", application);

        Config config = new Config();
        config.setApplications(applicationsMap);

        when(customApplicationConformToTypeSchemaValidator.isValid(
                eq(application),
                any(CustomApplicationConformToTypeSchemaValidationContext.class))
        ).thenReturn(true);

        // when
        normalizer.normalize(config);

        // then
        assertThat(config.getApplications()).hasSize(1).satisfies(applications -> {
            CoreApplication coreApplication = applications.get("testApplication");
            assertThat(coreApplication).isNotNull();
        });
    }

}