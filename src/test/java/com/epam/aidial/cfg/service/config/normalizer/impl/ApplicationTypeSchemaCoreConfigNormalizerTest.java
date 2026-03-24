package com.epam.aidial.cfg.service.config.normalizer.impl;

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
        String invalidApplicationTypeSchema = """
                {
                    "properties": {},
                    "applications": [],
                    "createdAt": "2025-08-29T11:34:26.709Z",
                    "updatedAt": "2025-08-29T11:34:26.709Z",
                    "$schema": "https://dial.epam.com/application_type_schemas/schema#",
                    "$id": "https://runner-char",
                    "$defs": {}
                }
                """;
        String validApplicationTypeSchema = """
                {
                    "properties": {},
                    "applications": [],
                    "createdAt": "2025-08-29T11:34:26.709Z",
                    "updatedAt": "2025-08-29T11:34:26.709Z",
                    "$schema": "https://dial.epam.com/application_type_schemas/schema#",
                    "$id": "https://runner-char-new",
                    "dial:applicationTypeDisplayName": "char1-new",
                    "dial:applicationTypeCompletionEndpoint": "https://app_hostname/openai/deployments/app_name/chat/completions",
                    "$defs": {}
                }
                """;

        Map<String, String> applicationTypeSchemasMap = new HashMap<>();
        applicationTypeSchemasMap.put("https://runner-char", invalidApplicationTypeSchema);
        applicationTypeSchemasMap.put("https://runner-char-new", validApplicationTypeSchema);

        CoreApplication application1 = new CoreApplication();
        application1.setApplicationTypeSchemaId(URI.create("https://runner-char"));

        CoreApplication application2 = new CoreApplication();
        application2.setApplicationTypeSchemaId(URI.create("https://runner-char"));

        CoreApplication application3 = new CoreApplication();
        application3.setApplicationTypeSchemaId(URI.create("https://runner-char-new"));

        CoreApplication application4 = new CoreApplication();
        application4.setApplicationTypeSchemaId(URI.create("https://runner-char-new"));

        CoreApplication application5 = new CoreApplication();
        application5.setEndpoint("https://endpoint");

        CoreApplication application6 = new CoreApplication();
        application6.setEndpoint("https://endpoint-new");

        Map<String, CoreApplication> applicationsMap = new HashMap<>();
        applicationsMap.put("testApplication1", application1);
        applicationsMap.put("testApplication2", application2);
        applicationsMap.put("testApplication3", application3);
        applicationsMap.put("testApplication4", application4);
        applicationsMap.put("testApplication5", application5);
        applicationsMap.put("testApplication6", application6);

        Config config = new Config();
        config.setApplicationTypeSchemas(applicationTypeSchemasMap);
        config.setApplications(applicationsMap);

        // when
        normalizer.normalize(config);

        // then
        assertThat(config.getApplicationTypeSchemas()).hasSize(1).satisfies(applicationTypeSchemas -> {
            assertThat(applicationTypeSchemas.get("https://runner-char")).isNull();
            assertThat(applicationTypeSchemas.get("https://runner-char-new")).isNotNull();
        });
        assertThat(config.getApplications()).hasSize(4).satisfies(applications -> {
            assertThat(applications.get("testApplication1")).isNull();
            assertThat(applications.get("testApplication2")).isNull();
            assertThat(applications.get("testApplication3")).isNotNull();
            assertThat(applications.get("testApplication4")).isNotNull();
            assertThat(applications.get("testApplication5")).isNotNull();
            assertThat(applications.get("testApplication6")).isNotNull();
        });
    }

    @Test
    void testNormalize_applicationTypeSchemaConformsToMetaSchema_doesNotRemoveApplicationTypeSchemaAndAssociatedApplications() {
        // given
        String validApplicationTypeSchema1 = """
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
        String validApplicationTypeSchema2 = """
                {
                    "properties": {},
                    "applications": [],
                    "createdAt": "2025-08-29T11:34:26.709Z",
                    "updatedAt": "2025-08-29T11:34:26.709Z",
                    "$schema": "https://dial.epam.com/application_type_schemas/schema#",
                    "$id": "https://runner-char-new",
                    "dial:applicationTypeDisplayName": "char1-new",
                    "dial:applicationTypeCompletionEndpoint": "https://app_hostname/openai/deployments/app_name/chat/completions",
                    "$defs": {}
                }
                """;

        Map<String, String> applicationTypeSchemasMap = new HashMap<>();
        applicationTypeSchemasMap.put("https://runner-char", validApplicationTypeSchema1);
        applicationTypeSchemasMap.put("https://runner-char-new", validApplicationTypeSchema2);

        CoreApplication application1 = new CoreApplication();
        application1.setApplicationTypeSchemaId(URI.create("https://runner-char"));

        CoreApplication application2 = new CoreApplication();
        application2.setApplicationTypeSchemaId(URI.create("https://runner-char"));

        CoreApplication application3 = new CoreApplication();
        application3.setApplicationTypeSchemaId(URI.create("https://runner-char-new"));

        CoreApplication application4 = new CoreApplication();
        application4.setApplicationTypeSchemaId(URI.create("https://runner-char-new"));

        CoreApplication application5 = new CoreApplication();
        application5.setEndpoint("https://endpoint");

        CoreApplication application6 = new CoreApplication();
        application6.setEndpoint("https://endpoint-new");

        Map<String, CoreApplication> applicationsMap = new HashMap<>();
        applicationsMap.put("testApplication1", application1);
        applicationsMap.put("testApplication2", application2);
        applicationsMap.put("testApplication3", application3);
        applicationsMap.put("testApplication4", application4);
        applicationsMap.put("testApplication5", application5);
        applicationsMap.put("testApplication6", application6);

        Config config = new Config();
        config.setApplicationTypeSchemas(applicationTypeSchemasMap);
        config.setApplications(applicationsMap);

        // when
        normalizer.normalize(config);

        // then
        assertThat(config.getApplicationTypeSchemas()).hasSize(2).satisfies(applicationTypeSchemas -> {
            assertThat(applicationTypeSchemas.get("https://runner-char")).isNotNull();
            assertThat(applicationTypeSchemas.get("https://runner-char-new")).isNotNull();
        });
        assertThat(config.getApplications()).hasSize(6).satisfies(applications -> {
            assertThat(applications.get("testApplication1")).isNotNull();
            assertThat(applications.get("testApplication2")).isNotNull();
            assertThat(applications.get("testApplication3")).isNotNull();
            assertThat(applications.get("testApplication4")).isNotNull();
            assertThat(applications.get("testApplication5")).isNotNull();
            assertThat(applications.get("testApplication6")).isNotNull();
        });
    }

}