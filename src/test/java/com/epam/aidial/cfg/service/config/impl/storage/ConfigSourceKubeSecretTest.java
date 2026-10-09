package com.epam.aidial.cfg.service.config.impl.storage;

import com.epam.aidial.cfg.service.config.transfer.VersionAwareFieldFilter;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreKey;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.function.Function;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConfigSourceKubeSecretTest {

    private static final String SECRET_NAME = "secret1";
    private static final String SECRET_KEY = "config";

    @Mock
    private VersionAwareFieldFilter versionAwareFieldFilter;
    @Mock
    private ConfigSplitter configSplitter;
    @Mock
    private ConfigMerger configMerger;
    @Mock
    private K8ConfigService k8ConfigService;
    @Mock
    private KubernetesClient kubernetesClient;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private ConfigSourceKubeSecret source;

    @Test
    void writeConfig_shouldUpdateExistingSecret() throws Exception {
        source = new ConfigSourceKubeSecret(versionAwareFieldFilter, configSplitter, configMerger,
                List.of(SECRET_NAME), 65536, k8ConfigService, SECRET_KEY, objectMapper);
        stubWithClient();

        Config config = sampleConfig();
        stubSplitter(config);

        source.writeConfig(config, false);

        verify(k8ConfigService, times(1))
                .updateSecretMapEntry(eq(kubernetesClient), eq(SECRET_NAME), eq(SECRET_KEY), any());
        verify(k8ConfigService, times(0))
                .createSecretMapEntry(any(), any(), any(), any());
    }

    @Test
    void writeConfig_shouldCreateSecretWhenNotFoundAndCreateResourcesTrue() throws Exception {
        source = new ConfigSourceKubeSecret(versionAwareFieldFilter, configSplitter, configMerger,
                List.of(SECRET_NAME), 65536, k8ConfigService, SECRET_KEY, objectMapper);
        stubWithClient();

        Config config = sampleConfig();
        stubSplitter(config);

        doThrow(new KubernetesClientException("secrets \"" + SECRET_NAME + "\" not found", 404, null))
                .when(k8ConfigService).updateSecretMapEntry(eq(kubernetesClient), eq(SECRET_NAME), eq(SECRET_KEY), any());

        source.writeConfig(config, true);

        verify(k8ConfigService, times(1))
                .createSecretMapEntry(eq(kubernetesClient), eq(SECRET_NAME), eq(SECRET_KEY), any());
    }

    @Test
    void writeConfig_shouldThrowWhenNotFoundAndCreateResourcesFalse() throws Exception {
        source = new ConfigSourceKubeSecret(versionAwareFieldFilter, configSplitter, configMerger,
                List.of(SECRET_NAME), 65536, k8ConfigService, SECRET_KEY, objectMapper);
        stubWithClient();

        Config config = sampleConfig();
        stubSplitter(config);

        doThrow(new KubernetesClientException("secrets \"" + SECRET_NAME + "\" not found", 404, null))
                .when(k8ConfigService).updateSecretMapEntry(eq(kubernetesClient), eq(SECRET_NAME), eq(SECRET_KEY), any());

        IllegalStateException exception = Assertions.assertThrows(IllegalStateException.class,
                () -> source.writeConfig(config, false));

        Assertions.assertTrue(exception.getMessage().contains("Secret is not found"));
        verify(k8ConfigService, times(0)).createSecretMapEntry(any(), any(), any(), any());
    }

    @Test
    void writeConfig_shouldRethrowOtherKubernetesExceptions() throws Exception {
        source = new ConfigSourceKubeSecret(versionAwareFieldFilter, configSplitter, configMerger,
                List.of(SECRET_NAME), 65536, k8ConfigService, SECRET_KEY, objectMapper);
        stubWithClient();

        Config config = sampleConfig();
        stubSplitter(config);

        doThrow(new KubernetesClientException("forbidden", 403, null))
                .when(k8ConfigService).updateSecretMapEntry(eq(kubernetesClient), eq(SECRET_NAME), eq(SECRET_KEY), any());

        Assertions.assertThrows(KubernetesClientException.class,
                () -> source.writeConfig(config, true));

        verify(k8ConfigService, times(0)).createSecretMapEntry(any(), any(), any(), any());
    }

    @SuppressWarnings("unchecked")
    private void stubWithClient() {
        when(k8ConfigService.withClient(any())).thenAnswer(invocation -> {
            Function<KubernetesClient, Object> task = invocation.getArgument(0);
            return task.apply(kubernetesClient);
        });
    }

    private Config sampleConfig() {
        Config config = new Config();
        CoreKey key = new CoreKey();
        key.setProject("project");
        key.setRole("role");
        config.setKeys(Map.of("key1", key));
        return config;
    }

    private void stubSplitter(Config config) throws Exception {
        ConfigPart configPart = new ConfigPart(config, objectMapper.writeValueAsString(config));
        when(configSplitter.splitConfig(any(), any(), anyInt(), eq(1))).thenReturn(List.of(configPart));
    }
}
