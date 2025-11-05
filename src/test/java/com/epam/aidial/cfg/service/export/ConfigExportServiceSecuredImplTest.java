package com.epam.aidial.cfg.service.export;

import com.epam.aidial.cfg.service.impl.storage.ConfigSource;
import com.epam.aidial.cfg.service.transfer.ConfigTransferLock;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreKey;
import com.epam.aidial.core.config.CoreModel;
import com.epam.aidial.core.config.CoreResourceAuthSettings;
import com.epam.aidial.core.config.CoreRole;
import com.epam.aidial.core.config.CoreToolSet;
import com.epam.aidial.core.config.CoreUpstream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ConfigExportServiceSecuredImplTest {

    @Mock
    private ConfigSource configSource;
    @Mock
    private ConfigSource securedConfigSource;
    @Mock
    private ConfigTransferLock configTransferLock;

    @Captor
    private ArgumentCaptor<Config> configCaptor;
    @Captor
    private ArgumentCaptor<Config> securedConfigCaptor;

    private ConfigExportServiceSecuredImpl configExportService;

    @BeforeEach
    void setUp() {
        configExportService = new ConfigExportServiceSecuredImpl(configSource, securedConfigSource, configTransferLock);
        doAnswer(invocation -> {
            Runnable runnable = invocation.getArgument(0);
            runnable.run();
            return null;
        }).when(configTransferLock).withWriteLock(any());
    }

    @Test
    void testExportWithCreateResources() {
        // given
        Config config = createTestConfig();
        boolean createResources = true;

        // when
        configExportService.export(config, createResources);

        // then
        verify(configSource).writeConfig(configCaptor.capture(), eq(createResources));
        verify(securedConfigSource).writeConfig(securedConfigCaptor.capture(), eq(createResources));
        verify(configTransferLock).withWriteLock(any());

        // Verify regular config has secrets removed
        Config regularConfig = configCaptor.getValue();
        assertTrue(regularConfig.getKeys().isEmpty());
        assertTrue(regularConfig.getModels().get("model1").getUpstreams().isEmpty());
        assertNull(regularConfig.getToolsets().get("toolset1").getAuthSettings().getClientSecret());

        // Verify secured config contains only secrets
        Config secretConfig = securedConfigCaptor.getValue();
        assertNotNull(secretConfig.getKeys().get("key1"));
        assertEquals("upstream-key-1", secretConfig.getModels().get("model1").getUpstreams().get(0).getKey());
        assertEquals("client-secret-1", secretConfig.getToolsets().get("toolset1").getAuthSettings().getClientSecret());
        assertTrue(secretConfig.getRoles().isEmpty());
    }

    @Test
    void testExportWithoutCreateResources() {
        // given
        Config config = createTestConfig();
        boolean createResources = false;

        // when
        configExportService.export(config, createResources);

        // then
        verify(configSource).writeConfig(configCaptor.capture(), eq(createResources));
        verify(securedConfigSource).writeConfig(securedConfigCaptor.capture(), eq(createResources));
        verify(configTransferLock).withWriteLock(any());

        // Verify regular config has secrets removed
        Config regularConfig = configCaptor.getValue();
        assertTrue(regularConfig.getKeys().isEmpty());
        assertTrue(regularConfig.getModels().get("model1").getUpstreams().isEmpty());
        assertNull(regularConfig.getToolsets().get("toolset1").getAuthSettings().getClientSecret());

        // Verify secured config contains only secrets
        Config secretConfig = securedConfigCaptor.getValue();
        assertNotNull(secretConfig.getKeys().get("key1"));
        assertEquals("upstream-key-1", secretConfig.getModels().get("model1").getUpstreams().get(0).getKey());
        assertEquals("client-secret-1", secretConfig.getToolsets().get("toolset1").getAuthSettings().getClientSecret());
        assertTrue(secretConfig.getRoles().isEmpty());
    }

    @Test
    void testExportWithEmptyConfig() {
        // given
        Config config = new Config();
        config.setRoles(new HashMap<>());
        config.setKeys(new HashMap<>());
        config.setModels(new HashMap<>());
        config.setToolsets(new HashMap<>());
        boolean createResources = true;

        // when
        configExportService.export(config, createResources);

        // then
        verify(configSource).writeConfig(configCaptor.capture(), eq(createResources));
        verify(securedConfigSource).writeConfig(securedConfigCaptor.capture(), eq(createResources));

        // Verify regular config remains unchanged
        Config regularConfig = configCaptor.getValue();
        assertTrue(regularConfig.getKeys().isEmpty());
        assertTrue(regularConfig.getModels().isEmpty());
        assertTrue(regularConfig.getToolsets().isEmpty());

        // Verify secured config is empty
        Config secretConfig = securedConfigCaptor.getValue();
        assertTrue(secretConfig.getKeys().isEmpty());
        assertTrue(secretConfig.getModels().isEmpty());
        assertTrue(secretConfig.getToolsets().isEmpty());
        assertTrue(secretConfig.getRoles().isEmpty());
    }

    @Test
    void testExportWithMixedSecrets() {
        // given
        Config config = createMixedSecretsConfig();
        boolean createResources = true;

        // when
        configExportService.export(config, createResources);

        // then
        verify(configSource).writeConfig(configCaptor.capture(), eq(createResources));
        verify(securedConfigSource).writeConfig(securedConfigCaptor.capture(), eq(createResources));

        // Verify regular config has secrets removed
        Config regularConfig = configCaptor.getValue();
        assertTrue(regularConfig.getKeys().isEmpty());
        assertTrue(regularConfig.getModels().get("model1").getUpstreams().isEmpty());
        assertNull(regularConfig.getToolsets().get("toolset1").getAuthSettings().getClientSecret());
        assertTrue(regularConfig.getModels().get("model2").getUpstreams().isEmpty());

        // Verify secured config contains only secrets
        Config secretConfig = securedConfigCaptor.getValue();
        assertNotNull(secretConfig.getKeys().get("key1"));
        assertEquals("upstream-key-1", secretConfig.getModels().get("model1").getUpstreams().get(0).getKey());
        assertEquals("upstream-key-2", secretConfig.getModels().get("model2").getUpstreams().get(0).getKey());
        assertEquals("https://api2.example.com", secretConfig.getModels().get("model2").getUpstreams().get(0).getEndpoint());
        assertEquals("client-secret-1", secretConfig.getToolsets().get("toolset1").getAuthSettings().getClientSecret());
        assertTrue(secretConfig.getRoles().isEmpty());
    }

    @Test
    void testExportWithNullClientSecret() {
        // given
        Config config = createTestConfig();
        config.getToolsets().get("toolset1").getAuthSettings().setClientSecret(null);
        boolean createResources = true;

        // when
        configExportService.export(config, createResources);

        // then
        verify(configSource).writeConfig(configCaptor.capture(), eq(createResources));
        verify(securedConfigSource).writeConfig(securedConfigCaptor.capture(), eq(createResources));

        // Verify secured config doesn't contain toolset with null client secret
        Config secretConfig = securedConfigCaptor.getValue();
        assertTrue(secretConfig.getToolsets().isEmpty());
    }

    @Test
    void testExportWithEmptyClientSecret() {
        // given
        Config config = createTestConfig();
        config.getToolsets().get("toolset1").getAuthSettings().setClientSecret("");
        boolean createResources = true;

        // when
        configExportService.export(config, createResources);

        // then
        verify(configSource).writeConfig(configCaptor.capture(), eq(createResources));
        verify(securedConfigSource).writeConfig(securedConfigCaptor.capture(), eq(createResources));

        // Verify secured config doesn't contain toolset with empty client secret
        Config secretConfig = securedConfigCaptor.getValue();
        assertTrue(secretConfig.getToolsets().isEmpty());
    }

    private Config createTestConfig() {
        Config config = new Config();

        CoreRole role = new CoreRole();
        role.setName("admin");
        Map<String, CoreRole> roles = new HashMap<>();
        roles.put("admin", role);
        config.setRoles(roles);

        CoreKey key = new CoreKey();

        Map<String, CoreKey> keys = new HashMap<>();
        keys.put("key1", key);
        config.setKeys(keys);

        Map<String, CoreModel> models = new HashMap<>();
        CoreModel model = new CoreModel();
        model.setName("model1");
        model.setDisplayName("Test Model");

        CoreUpstream upstream = new CoreUpstream();
        upstream.setEndpoint("https://api.example.com");
        upstream.setKey("upstream-key-1");
        model.setUpstreams(List.of(upstream));
        
        models.put("model1", model);
        config.setModels(models);

        Map<String, CoreToolSet> toolsets = new HashMap<>();
        CoreToolSet toolSet = new CoreToolSet();
        toolSet.setName("toolset1");
        toolSet.setDisplayName("Test ToolSet");

        CoreResourceAuthSettings authSettings = new CoreResourceAuthSettings();
        authSettings.setClientSecret("client-secret-1");
        toolSet.setAuthSettings(authSettings);
        
        toolsets.put("toolset1", toolSet);
        config.setToolsets(toolsets);

        return config;
    }

    private Config createMixedSecretsConfig() {
        Config config = createTestConfig();
        
        // Add a second model with both secret and non-secret data
        CoreModel model2 = new CoreModel();
        model2.setName("model2");
        model2.setDisplayName("Test Model 2");
        
        CoreUpstream upstream2 = new CoreUpstream();
        upstream2.setEndpoint("https://api2.example.com");
        upstream2.setKey("upstream-key-2");
        model2.setUpstreams(List.of(upstream2));
        
        config.getModels().put("model2", model2);
        
        // Add a toolset without secrets
        CoreToolSet toolSet2 = new CoreToolSet();
        toolSet2.setName("toolset2");
        toolSet2.setDisplayName("Test ToolSet 2");
        toolSet2.setAuthSettings(new CoreResourceAuthSettings());
        
        config.getToolsets().put("toolset2", toolSet2);
        
        return config;
    }
}