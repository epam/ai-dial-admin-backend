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
import com.epam.aidial.core.config.CoreToolSet;
import com.fasterxml.jackson.core.JsonProcessingException;
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
import java.util.stream.Collectors;

class ConfigSplitterTest {

    private ObjectMapper objectMapper = JsonMapperConfiguration.createJsonMapper();

    @Test
    void splitSecretConfig() {
        ConfigSplitter splitter = new ConfigSplitter();
        Config configBody = new Config();
        configBody.setKeys(new HashMap<>());
        for (int i = 0; i < 5; i++) {
            CoreKey key = generateKey(i);
            configBody.getKeys().put("key" + i, key);
        }
        List<ConfigPart> secretConfigs = splitter.splitConfig(configBody, this::encode, 180, 5);
        Assertions.assertEquals(2, secretConfigs.size());
        Assertions.assertEquals(Set.of("key0", "key1", "key2"), secretConfigs.get(0).config().getKeys().keySet());
        Assertions.assertEquals(Set.of("key3", "key4"), secretConfigs.get(1).config().getKeys().keySet());
    }

    @Test
    void splitConfig() throws JsonProcessingException {
        ConfigSplitter splitter = new ConfigSplitter();
        Config configBody = new Config();
        configBody.getRoutes().put("route1", generateRoute());
        configBody.getModels().put("model1", generateModel());
        configBody.getAddons().put("addon1", generateAddon());
        configBody.getApplications().put("application1", generateApplication());
        configBody.getAssistant().setEndpoint("endpoint1");
        configBody.getAssistant().setFeatures(new CoreFeatures());
        configBody.getAssistant().getFeatures().setAllowResume(true);
        configBody.getAssistant().getAssistants().put("assistant1", generateAssistant());
        configBody.setKeys(new HashMap<>());
        for (int i = 0; i < 5; i++) {
            CoreKey key = generateKey(i);
            configBody.getKeys().put("key" + i, key);
        }
        configBody.getRoles().put("role1", generateRole());
        configBody.getRetriableErrorCodes().add(10);
        configBody.getInterceptors().put("interceptor1", generateInterceptor());
        configBody.getApplicationTypeSchemas().put("schema1", generateSchema());
        configBody.getToolsets().put("toolset1", generateToolSet());

        List<ConfigPart> splittedConfig = splitter.splitConfig(configBody, this::encode, 400, 10);

        Assertions.assertEquals(9, splittedConfig.size());
        List<Config> expected = expected();
        List<Config> actual = splittedConfig.stream()
                .map(ConfigPart::config)
                .collect(Collectors.toList());
        Assertions.assertEquals(expected, actual);
    }

    private List<Config> expected() {
        List<Config> configs = new ArrayList<>();
        Config config = createConfig();
        config.setRoutes(new LinkedHashMap<>(Map.of("route1", generateRoute())));
        config.setModels(Map.of("model1", generateModel()));
        configs.add(config);

        config = createConfig();
        config.setAddons(Map.of("addon1", generateAddon()));
        config.setApplications(Map.of("application1", generateApplication()));
        configs.add(config);

        config = createConfig();
        config.setAssistant(new Assistants());
        config.getAssistant().setEndpoint("endpoint1");
        CoreFeatures features = new CoreFeatures();
        features.setAllowResume(true);
        config.getAssistant().setFeatures(features);
        config.getAssistant().setAssistants(null);
        configs.add(config);

        config = createConfig();
        config.setAssistant(new Assistants());
        config.getAssistant().setAssistants(Map.of("assistant1", generateAssistant()));
        config.setKeys(Map.of("key1", generateKey(1)));
        configs.add(config);

        config = createConfig();
        config.setKeys(Map.of("key2", generateKey(2), "key0", generateKey(0)));
        configs.add(config);

        config = createConfig();
        config.setKeys(Map.of("key3", generateKey(3), "key4", generateKey(4)));
        configs.add(config);

        config = createConfig();
        config.setRoles(Map.of("role1", generateRole()));
        config.setRetriableErrorCodes(Set.of(10));
        configs.add(config);

        config = createConfig();
        config.setInterceptors(Map.of("interceptor1", generateInterceptor()));
        config.setApplicationTypeSchemas(Map.of("schema1", "schema1"));
        configs.add(config);

        config = createConfig();
        config.setToolsets(Map.of("toolset1", generateToolSet()));
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
        config.setToolsets(null);
        return config;
    }

    private String generateSchema() {
        return "schema1";
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

    private CoreToolSet generateToolSet() {
        CoreToolSet toolSet = new CoreToolSet();
        toolSet.setName("toolset1");
        return toolSet;
    }

    @SneakyThrows
    private String encode(Object body) {
        return objectMapper.writeValueAsString(body);
    }
}