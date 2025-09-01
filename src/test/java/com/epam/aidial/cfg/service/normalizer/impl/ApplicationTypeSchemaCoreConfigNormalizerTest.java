package com.epam.aidial.cfg.service.normalizer.impl;

import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreApplication;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ApplicationTypeSchemaCoreConfigNormalizerTest {

    private ApplicationTypeSchemaCoreConfigNormalizer normalizer;

    @BeforeEach
    void init() {
        normalizer = new ApplicationTypeSchemaCoreConfigNormalizer();
    }

    @Test
    void testNormalize_applicationTypeSchemasMissingInConfig_doNothing() {
        // given
        Config config = new Config();

        // when
        normalizer.normalize(config);

        // then
        assertThat(config.getApplicationTypeSchemas()).isEmpty();
    }

    @Test
    void testNormalize_applicationTypeSchemaDoesNotConformToMetaSchema_removeApplicationTypeSchemaAndAssociatedApplications() {
        // given
        String applicationTypeSchema = """
                {
                    "properties": {},
                    "applications": [],
                    "createdAt": "2025-08-29T11:34:26.709Z",
                    "updatedAt": "2025-08-29T11:34:26.709Z",
                    "$schema": "https://dial.epam.com/application_type_schemas/schema#",
                    "$id": "https://runner-char",
                    "dial:applicationTypeDisplayName": "char1",
                    "$defs": {}
                }
                """;

        Map<String, String> applicationTypeSchemasMap = new HashMap<>();
        applicationTypeSchemasMap.put("https://runner-char", applicationTypeSchema);

        CoreApplication application1 = new CoreApplication();
        application1.setApplicationTypeSchemaId(URI.create("https://runner-char"));

        CoreApplication application2 = new CoreApplication();
        application2.setApplicationTypeSchemaId(URI.create("https://runner-char-new"));

        CoreApplication application3 = new CoreApplication();
        application3.setEndpoint("https://endpoint");

        Map<String, CoreApplication> applicationsMap = new HashMap<>();
        applicationsMap.put("testApplication1", application1);
        applicationsMap.put("testApplication2", application2);
        applicationsMap.put("testApplication3", application3);

        Config config = new Config();
        config.setApplicationTypeSchemas(applicationTypeSchemasMap);
        config.setApplications(applicationsMap);

        // when
        normalizer.normalize(config);

        // then
        assertThat(config.getApplicationTypeSchemas()).isEmpty();
        assertThat(config.getApplications()).hasSize(2).satisfies(applications -> {
            assertThat(applications.get("testApplication1")).isNull();
            assertThat(applications.get("testApplication2")).isNotNull();
            assertThat(applications.get("testApplication3")).isNotNull();
        });
    }

    @Test
    void testNormalize_applicationTypeSchemaConformsToMetaSchema_doesNotRemoveApplicationTypeSchemaAndAssociatedApplications() {
        // given
        String applicationTypeSchema = """
                {
                    "properties": {},
                    "applications": [],
                    "createdAt": "2025-08-29T11:34:26.709Z",
                    "updatedAt": "2025-08-29T11:34:26.709Z",
                    "$schema": "https://dial.epam.com/application_type_schemas/schema#",
                    "$id": "https://runner-char",
                    "dial:applicationTypeDisplayName": "char1",
                    "dial:applicationTypeCompletionEndpoint": "https://app_hostname/openai/deployments/app_name/chat/completions",
                    "$defs": {}
                }
                """;

        Map<String, String> applicationTypeSchemasMap = new HashMap<>();
        applicationTypeSchemasMap.put("https://runner-char", applicationTypeSchema);

        CoreApplication application1 = new CoreApplication();
        application1.setApplicationTypeSchemaId(URI.create("https://runner-char"));

        CoreApplication application2 = new CoreApplication();
        application2.setApplicationTypeSchemaId(URI.create("https://runner-char-new"));

        CoreApplication application3 = new CoreApplication();
        application3.setEndpoint("https://endpoint");

        Map<String, CoreApplication> applicationsMap = new HashMap<>();
        applicationsMap.put("testApplication1", application1);
        applicationsMap.put("testApplication2", application2);
        applicationsMap.put("testApplication3", application3);

        Config config = new Config();
        config.setApplicationTypeSchemas(applicationTypeSchemasMap);
        config.setApplications(applicationsMap);

        // when
        normalizer.normalize(config);

        // then
        assertThat(config.getApplicationTypeSchemas()).hasSize(1).satisfies(applicationTypeSchemas -> {
            String schema = applicationTypeSchemas.get("https://runner-char");
            assertThat(schema).isNotNull();
        });
        assertThat(config.getApplications()).hasSize(3).satisfies(applications -> {
            assertThat(applications.get("testApplication1")).isNotNull();
            assertThat(applications.get("testApplication2")).isNotNull();
            assertThat(applications.get("testApplication3")).isNotNull();
        });
    }

}