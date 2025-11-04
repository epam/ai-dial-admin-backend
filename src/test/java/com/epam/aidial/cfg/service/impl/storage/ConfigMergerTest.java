package com.epam.aidial.cfg.service.impl.storage;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.core.config.Assistants;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreAddon;
import com.epam.aidial.core.config.CoreApplication;
import com.epam.aidial.core.config.CoreAssistant;
import com.epam.aidial.core.config.CoreFeatures;
import com.epam.aidial.core.config.CoreInterceptor;
import com.epam.aidial.core.config.CoreKey;
import com.epam.aidial.core.config.CoreModel;
import com.epam.aidial.core.config.CoreRole;
import com.epam.aidial.core.config.CoreRoute;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class ConfigMergerTest {

    private final ObjectMapper objectMapper = JsonMapperConfiguration.createJsonMapper();
    private final ConfigMerger merger = new ConfigMerger();

    @Test
    void mergeEmptyConfigs() {
        // Test merging empty list of configs
        List<String> emptyList = new ArrayList<>();
        Config result = merger.merge(emptyList, this::decode);

        Assertions.assertNotNull(result);
        // In Config class, collections are initialized with empty collections by default
        Assertions.assertTrue(result.getRoutes().isEmpty());
        Assertions.assertTrue(result.getModels().isEmpty());
        Assertions.assertTrue(result.getAddons().isEmpty());
        Assertions.assertTrue(result.getApplications().isEmpty());
        Assertions.assertNotNull(result.getAssistant());
        Assertions.assertTrue(result.getKeys().isEmpty());
        Assertions.assertTrue(result.getRoles().isEmpty());
        Assertions.assertTrue(result.getInterceptors().isEmpty());
        Assertions.assertTrue(result.getApplicationTypeSchemas().isEmpty());
        Assertions.assertTrue(result.getRetriableErrorCodes().isEmpty());
    }

    @Test
    void mergeConfigsWithKeys() {
        // Create configs with keys
        List<String> encodedConfigs = new ArrayList<>();

        // First config with keys 0, 1, 2
        Config config1 = createConfig();
        config1.setKeys(new HashMap<>());
        for (int i = 0; i < 3; i++) {
            CoreKey key = generateKey(i);
            config1.getKeys().put("key" + i, key);
        }
        encodedConfigs.add(encode(config1));

        // Second config with keys 3, 4
        Config config2 = createConfig();
        config2.setKeys(new HashMap<>());
        for (int i = 3; i < 5; i++) {
            CoreKey key = generateKey(i);
            config2.getKeys().put("key" + i, key);
        }
        encodedConfigs.add(encode(config2));

        // Merge configs
        Config result = merger.merge(encodedConfigs, this::decode);

        // Verify result
        Assertions.assertNotNull(result.getKeys());
        Assertions.assertEquals(5, result.getKeys().size());
        for (int i = 0; i < 5; i++) {
            String keyName = "key" + i;
            String projectName = "project" + i;
            Assertions.assertTrue(result.getKeys().containsKey(keyName));
            Assertions.assertEquals(projectName, result.getKeys().get(keyName).getProject());
        }
    }

    @Test
    void mergeCompleteConfigs() {
        List<String> encodedConfigs = new ArrayList<>();
        List<Config> configs = createTestConfigs();

        // Encode all configs
        for (Config config : configs) {
            encodedConfigs.add(encode(config));
        }

        // Merge configs
        Config result = merger.merge(encodedConfigs, this::decode);

        // Verify routes
        Assertions.assertNotNull(result.getRoutes());
        Assertions.assertEquals(1, result.getRoutes().size());
        Assertions.assertTrue(result.getRoutes().containsKey("route1"));
        Assertions.assertEquals("route1", result.getRoutes().get("route1").getName());

        // Verify models
        Assertions.assertNotNull(result.getModels());
        Assertions.assertEquals(1, result.getModels().size());
        Assertions.assertTrue(result.getModels().containsKey("model1"));
        Assertions.assertEquals("model1", result.getModels().get("model1").getName());

        // Verify addons
        Assertions.assertNotNull(result.getAddons());
        Assertions.assertEquals(1, result.getAddons().size());
        Assertions.assertTrue(result.getAddons().containsKey("addon1"));
        Assertions.assertEquals("addon1", result.getAddons().get("addon1").getName());

        // Verify applications
        Assertions.assertNotNull(result.getApplications());
        Assertions.assertEquals(1, result.getApplications().size());
        Assertions.assertTrue(result.getApplications().containsKey("application1"));
        Assertions.assertEquals("application1", result.getApplications().get("application1").getName());

        // Verify assistants
        Assertions.assertNotNull(result.getAssistant());
        Assertions.assertEquals("endpoint1", result.getAssistant().getEndpoint());
        Assertions.assertNotNull(result.getAssistant().getFeatures());
        Assertions.assertTrue(result.getAssistant().getFeatures().getAllowResume());
        Assertions.assertNotNull(result.getAssistant().getAssistants());
        Assertions.assertEquals(1, result.getAssistant().getAssistants().size());
        Assertions.assertTrue(result.getAssistant().getAssistants().containsKey("assistant1"));
        Assertions.assertEquals("assistant1", result.getAssistant().getAssistants().get("assistant1").getName());

        // Verify keys
        Assertions.assertNotNull(result.getKeys());
        Assertions.assertEquals(5, result.getKeys().size());
        for (int i = 0; i < 5; i++) {
            String keyName = "key" + i;
            String projectName = "project" + i;
            Assertions.assertTrue(result.getKeys().containsKey(keyName));
            Assertions.assertEquals(projectName, result.getKeys().get(keyName).getProject());
        }

        // Verify roles
        Assertions.assertNotNull(result.getRoles());
        Assertions.assertEquals(1, result.getRoles().size());
        Assertions.assertTrue(result.getRoles().containsKey("role1"));
        Assertions.assertEquals("role1", result.getRoles().get("role1").getName());

        // Verify retriable error codes
        Assertions.assertNotNull(result.getRetriableErrorCodes());
        Assertions.assertEquals(1, result.getRetriableErrorCodes().size());
        Assertions.assertTrue(result.getRetriableErrorCodes().contains(10));

        // Verify interceptors
        Assertions.assertNotNull(result.getInterceptors());
        Assertions.assertEquals(1, result.getInterceptors().size());
        Assertions.assertTrue(result.getInterceptors().containsKey("interceptor1"));
        Assertions.assertEquals("interceptor1", result.getInterceptors().get("interceptor1").getName());

        // Verify application type schemas
        Assertions.assertNotNull(result.getApplicationTypeSchemas());
        Assertions.assertEquals(2, result.getApplicationTypeSchemas().size());
        Assertions.assertTrue(result.getApplicationTypeSchemas().containsKey("schema1"));
        Assertions.assertTrue(result.getApplicationTypeSchemas().containsKey("schema2"));
    }

    private List<Config> createTestConfigs() {
        List<Config> configs = new ArrayList<>();

        // Config 1: Routes and Models
        Config config = createConfig();
        config.setRoutes(new LinkedHashMap<>(Map.of("route1", generateRoute())));
        config.setModels(Map.of("model1", generateModel()));
        configs.add(config);

        // Config 2: Addons and Applications
        config = createConfig();
        config.setAddons(Map.of("addon1", generateAddon()));
        config.setApplications(Map.of("application1", generateApplication()));
        configs.add(config);

        // Config 3: Assistant endpoint and features
        config = createConfig();
        config.setAssistant(new Assistants());
        config.getAssistant().setEndpoint("endpoint1");
        CoreFeatures features = new CoreFeatures();
        features.setAllowResume(true);
        config.getAssistant().setFeatures(features);
        configs.add(config);

        // Config 4: Assistant assistants and key1
        config = createConfig();
        config.setAssistant(new Assistants());
        config.getAssistant().setAssistants(Map.of("assistant1", generateAssistant()));
        config.setKeys(Map.of("key1", generateKey(1)));
        configs.add(config);

        // Config 5: Keys 0 and 2
        config = createConfig();
        config.setKeys(Map.of("key0", generateKey(0), "key2", generateKey(2)));
        configs.add(config);

        // Config 6: Keys 3 and 4
        config = createConfig();
        config.setKeys(Map.of("key3", generateKey(3), "key4", generateKey(4)));
        configs.add(config);

        // Config 7: Roles and retriable error codes
        config = createConfig();
        config.setRoles(Map.of("role1", generateRole()));
        config.setRetriableErrorCodes(Set.of(10));
        configs.add(config);

        // Config 8: Interceptors and first schema
        config = createConfig();
        config.setInterceptors(Map.of("interceptor1", generateInterceptor()));
        config.setApplicationTypeSchemas(Map.of("schema1", generateValidSchema1()));
        configs.add(config);

        // Config 9: Second schema
        config = createConfig();
        config.setApplicationTypeSchemas(Map.of("schema2", generateValidSchema2()));
        configs.add(config);

        return configs;
    }

    private Config createConfig() {
        Config config = new Config();
        config.setRoutes(null);
        config.setModels(null);
        config.setAddons(null);
        config.setApplications(null);
        config.setAssistant(null);
        config.setKeys(null);
        config.setRoles(null);
        config.setInterceptors(null);
        config.setApplicationTypeSchemas(null);
        config.setRetriableErrorCodes(null);
        return config;
    }


    /**
     * Generates a valid application schema that passes validation.
     * The schema must:
     * 1. Have a $id property
     * 2. Have required DIAL properties
     * 3. Conform to the meta-schema
     */
    private String generateValidSchema1() {
        return """
                {
                    "$id": "schema1",
                    "$schema": "http://json-schema.org/draft-07/schema#",
                    "type": "object",
                    "dial:applicationTypeEditorUrl": "https://example.com/editor",
                    "dial:applicationTypeCompletionEndpoint": "https://example.com/completion",
                    "dial:applicationTypeDisplayName": "Test Schema 1",
                    "properties": {
                        "name": {
                            "type": "string",
                            "dial:meta": {
                                "dial:propertyOrder": 1,
                                "dial:propertyKind": "client"
                            }
                        }
                    }
                }""";
    }

    /**
     * Generates another valid application schema for testing merging.
     */
    private String generateValidSchema2() {
        return """
                {
                    "$id": "schema2",
                    "$schema": "http://json-schema.org/draft-07/schema#",
                    "type": "object",
                    "dial:applicationTypeEditorUrl": "https://example.com/editor2",
                    "dial:applicationTypeCompletionEndpoint": "https://example.com/completion2",
                    "dial:applicationTypeDisplayName": "Test Schema 2",
                    "properties": {
                        "description": {
                            "type": "string",
                            "dial:meta": {
                                "dial:propertyOrder": 1,
                                "dial:propertyKind": "client"
                            }
                        }
                    }
                }""";
    }

    private CoreInterceptor generateInterceptor() {
        CoreInterceptor coreInterceptor = new CoreInterceptor();
        coreInterceptor.setName("interceptor1");
        return coreInterceptor;
    }

    private CoreRole generateRole() {
        CoreRole coreRole = new CoreRole();
        coreRole.setName("role1");
        return coreRole;
    }

    private CoreApplication generateApplication() {
        CoreApplication coreApplication = new CoreApplication();
        coreApplication.setName("application1");
        return coreApplication;
    }

    private CoreAddon generateAddon() {
        CoreAddon coreAddon = new CoreAddon();
        coreAddon.setName("addon1");
        return coreAddon;
    }

    private CoreModel generateModel() {
        CoreModel coreModel = new CoreModel();
        coreModel.setName("model1");
        return coreModel;
    }

    private CoreRoute generateRoute() {
        CoreRoute coreRoute = new CoreRoute();
        coreRoute.setName("route1");
        return coreRoute;
    }

    private CoreAssistant generateAssistant() {
        CoreAssistant coreAssistant = new CoreAssistant();
        coreAssistant.setName("assistant1");
        return coreAssistant;
    }

    private CoreKey generateKey(int i) {
        CoreKey key = new CoreKey();
        key.setProject("project" + i);
        return key;
    }

    @SneakyThrows
    private String encode(Object body) {
        return objectMapper.writeValueAsString(body);
    }

    @SneakyThrows
    private Config decode(String encoded) {
        return objectMapper.readValue(encoded, Config.class);
    }
}