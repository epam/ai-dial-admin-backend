package com.epam.aidial.cfg.service.config.impl.storage;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AzureKeyVaultConfigSourceTest {

    @Mock
    private SecretClient secretClient;
    @Captor
    private ArgumentCaptor<KeyVaultSecret> keyVaultSecretArgumentCaptor;
    private String expirationTimeUnit = "MONTHS";
    private long expirationPeriod = 3;
    private ObjectMapper objectMapper = new ObjectMapper();
    private AzureKeyVaultConfigSource source;
    @Mock
    private ConfigSplitter configSplitter;
    @Mock
    private ConfigMerger configMerger;

    @Test
    void writeConfig_shouldWriteSecret() throws JsonProcessingException {
        source = new AzureKeyVaultConfigSource(configSplitter, configMerger, List.of("secret1"),
            secretClient, expirationTimeUnit, expirationPeriod, objectMapper);

        Config config = new Config();
        CoreKey key = new CoreKey();
        key.setProject("project");
        key.setRole("role");
        config.setKeys(Map.of("key1", key));

        when(secretClient.getSecret(any(String.class))).thenReturn(new KeyVaultSecret("secret", "{}"));
        Config secretConfig = new Config();
        secretConfig.setKeys(Map.of("key1", key));

        source.writeConfig(config, false);

        verify(configSplitter, times(0)).splitConfig(any(), any(), anyInt(), eq(1));
        verify(secretClient, times(1)).setSecret(keyVaultSecretArgumentCaptor.capture());

        List<KeyVaultSecret> allValues = keyVaultSecretArgumentCaptor.getAllValues();
        Assertions.assertEquals(allValues.size(), 1);
        Assertions.assertEquals(allValues.get(0).getName(), "secret1");
        Assertions.assertEquals(objectMapper.readValue(allValues.get(0).getValue(), Config.class), config);
    }

    @Test
    void writeConfig_shouldWriteWithoutSplit() throws JsonProcessingException {
        source = new AzureKeyVaultConfigSource(configSplitter, configMerger, List.of("secret1", "secret2"),
            secretClient, expirationTimeUnit, expirationPeriod, objectMapper);

        Config config = new Config();
        CoreKey key = new CoreKey();
        key.setProject("project");
        key.setRole("role");
        config.setKeys(Map.of("key1", key));

        when(secretClient.getSecret(any(String.class))).thenReturn(new KeyVaultSecret("secret", "{}"));
        Config secretConfig = new Config();
        secretConfig.setKeys(Map.of("key1", key));
        ConfigPart configPart = new ConfigPart(secretConfig, objectMapper.writeValueAsString(secretConfig));
        when(configSplitter.splitConfig(any(), any(), anyInt(), eq(2))).thenReturn(List.of(configPart));

        source.writeConfig(config, false);

        verify(secretClient, times(2)).setSecret(keyVaultSecretArgumentCaptor.capture());

        List<KeyVaultSecret> allValues = keyVaultSecretArgumentCaptor.getAllValues();
        Assertions.assertEquals(allValues.size(), 2);
        Assertions.assertEquals(allValues.get(0).getName(), "secret1");
        Assertions.assertEquals(objectMapper.readValue(allValues.get(0).getValue(), Config.class), config);
        Assertions.assertEquals(allValues.get(1).getName(), "secret2");
        Assertions.assertEquals(objectMapper.readValue(allValues.get(1).getValue(), Config.class), new Config());
    }

    @Test
    void writeConfig_shouldThrowExceptionNotEnoughSpace() throws JsonProcessingException {
        source = new AzureKeyVaultConfigSource(configSplitter, configMerger, List.of("secret1", "secret2"),
            secretClient, expirationTimeUnit, expirationPeriod, objectMapper);

        Config config = new Config();
        CoreKey key = new CoreKey();
        key.setProject("project");
        key.setRole("role");
        config.setKeys(Map.of("key1", key));

        Config secretConfig = new Config();
        secretConfig.setKeys(Map.of("key1", key));
        ConfigPart configPart = new ConfigPart(secretConfig, objectMapper.writeValueAsString(secretConfig));

        when(configSplitter.splitConfig(any(), any(), anyInt(), eq(2))).thenReturn(List.of(configPart, emptyConfigPart(), emptyConfigPart()));

        Assertions.assertThrows(Exception.class, () -> source.writeConfig(config, false));
    }

    @Test
    void writeConfig_shouldCreateResourceWhenTrue() throws JsonProcessingException {
        source = new AzureKeyVaultConfigSource(configSplitter, configMerger, List.of("secret1"),
            secretClient, expirationTimeUnit, expirationPeriod, objectMapper);

        Config config = new Config();
        CoreKey key = new CoreKey();
        key.setProject("project");
        key.setRole("role");
        config.setKeys(Map.of("key1", key));

        when(secretClient.getSecret(any(String.class)))
                .thenThrow(new ResourceNotFoundException("Secret not found", null));

        source.writeConfig(config, true);

        verify(secretClient, times(1)).setSecret(keyVaultSecretArgumentCaptor.capture());

        List<KeyVaultSecret> allValues = keyVaultSecretArgumentCaptor.getAllValues();
        Assertions.assertEquals(1, allValues.size());
        Assertions.assertEquals("secret1", allValues.get(0).getName());
        Assertions.assertEquals(objectMapper.readValue(allValues.get(0).getValue(), Config.class), config);
    }

    @Test
    void writeConfig_shouldThrowExceptionWhenResourceNotFoundAndCreateResourcesFalse() {
        source = new AzureKeyVaultConfigSource(configSplitter, configMerger, List.of("secret1"),
            secretClient, expirationTimeUnit, expirationPeriod, objectMapper);

        Config config = new Config();
        CoreKey key = new CoreKey();
        key.setProject("project");
        key.setRole("role");
        config.setKeys(Map.of("key1", key));

        when(secretClient.getSecret(any(String.class)))
                .thenThrow(new ResourceNotFoundException("Secret not found", null));

        IllegalStateException exception = Assertions.assertThrows(
                IllegalStateException.class,
                () -> source.writeConfig(config, false)
        );

        Assertions.assertTrue(exception.getMessage().contains("Secret is not found"));
    }

    @SneakyThrows
    private ConfigPart emptyConfigPart() {
        Config config = new Config();
        return new ConfigPart(config, objectMapper.writeValueAsString(config));
    }

}