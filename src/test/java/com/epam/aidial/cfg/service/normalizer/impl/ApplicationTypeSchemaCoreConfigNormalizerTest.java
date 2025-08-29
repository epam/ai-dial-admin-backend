package com.epam.aidial.cfg.service.normalizer.impl;

import com.epam.aidial.core.config.Config;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

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
    void testNormalize_applicationTypeSchemaDoesNotConformToMetaSchema_removeApplicationTypeSchema() {
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
                     "dial:applicationTypeRoutes": [
                         {
                             "isPublic": false,
                             "name": "char_route",
                             "rewritePath": false,
                             "paths": [
                                 "/auto-test-global-route/some/suffix/here",
                                 ""
                             ],
                             "methods": [
                                 "POST",
                                 "GET"
                             ],
                             "upstreams": [
                                 {
                                     "weight": 1,
                                     "tier": 0
                                 }
                             ],
                             "maxRetryAttempts": 1,
                             "order": 1,
                             "permissions": [
                                 "read",
                                 "write"
                             ]
                         }
                     ],
                     "$defs": {}
                 }
                """;

        Map<String, String> applicationTypeSchemasMap = new HashMap<>();
        applicationTypeSchemasMap.put("https://runner-char", applicationTypeSchema);

        Config config = new Config();
        config.setApplicationTypeSchemas(applicationTypeSchemasMap);

        // when
        normalizer.normalize(config);

        // then
        assertThat(config.getApplications()).isEmpty();
    }

    @Test
    void testNormalize_applicationTypeSchemaConformsToMetaSchema_doesNotRemoveApplicationTypeSchema() {
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

        Config config = new Config();
        config.setApplicationTypeSchemas(applicationTypeSchemasMap);

        // when
        normalizer.normalize(config);

        // then
        assertThat(config.getApplicationTypeSchemas()).hasSize(1).satisfies(applicationTypeSchemas -> {
            String schema = applicationTypeSchemas.get("https://runner-char");
            assertThat(schema).isNotNull();
        });
    }

}