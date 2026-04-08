package com.epam.aidial.cfg.service.config.impl.storage;

import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.httpjson.HttpJsonStatusCode;
import com.google.api.gax.rpc.NotFoundException;
import com.google.api.gax.rpc.StatusCode;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.protobuf.ByteString;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GcpVaultConfigSourceTest {

    @Mock
    private SecretManagerServiceClient secretClient;
    @Captor
    private ArgumentCaptor<SecretPayload> keyVaultSecretArgumentCaptor;
    private ObjectMapper objectMapper = new ObjectMapper();
    private GcpVaultConfigSource source;
    @Mock
    private ConfigSplitter configSplitter;
    @Mock
    private ConfigMerger configMerger;

    @Test
    void writeConfig_shouldWriteSecret() throws JsonProcessingException, UnsupportedEncodingException {
        source = new GcpVaultConfigSource(secretClient, configSplitter, configMerger, List.of("secret1"), objectMapper, "projectId");

        Config config = new Config();
        CoreKey key = new CoreKey();
        key.setProject("project");
        key.setRole("role");
        config.setKeys(Map.of("key1", key));

        ByteString body = ByteString.copyFrom("{}", "utf-8");
        SecretPayload payload = SecretPayload.newBuilder().setData(body).build();
        AccessSecretVersionResponse response = AccessSecretVersionResponse.newBuilder().setPayload(payload).build();
        when(secretClient.accessSecretVersion(any(SecretVersionName.class))).thenReturn(response);
        Config secretConfig = new Config();
        secretConfig.setKeys(Map.of("key1", key));

        source.writeConfig(config, false);

        verify(configSplitter, times(0)).splitConfig(any(), any(), anyInt(), eq(1));
        verify(secretClient, times(1)).addSecretVersion(Mockito.eq("secret1"), keyVaultSecretArgumentCaptor.capture());

        List<SecretPayload> allValues = keyVaultSecretArgumentCaptor.getAllValues();
        Assertions.assertEquals(allValues.size(), 1);
        Assertions.assertEquals(objectMapper.readValue(allValues.get(0).getData().toStringUtf8(), Config.class), config);
    }

    @Test
    void writeConfig_shouldWriteWithoutSplit() throws JsonProcessingException, UnsupportedEncodingException {
        source = new GcpVaultConfigSource(secretClient, configSplitter, configMerger, List.of("secret1", "secret2"), objectMapper, "projectId");

        Config config = new Config();
        CoreKey key = new CoreKey();
        key.setProject("project");
        key.setRole("role");
        config.setKeys(Map.of("key1", key));

        ByteString body = ByteString.copyFrom("{}", "utf-8");
        SecretPayload payload = SecretPayload.newBuilder().setData(body).build();
        AccessSecretVersionResponse response = AccessSecretVersionResponse.newBuilder().setPayload(payload).build();
        when(secretClient.accessSecretVersion(any(SecretVersionName.class))).thenReturn(response);
        Config secretConfig = new Config();
        secretConfig.setKeys(Map.of("key1", key));
        ConfigPart configPart = new ConfigPart(secretConfig, objectMapper.writeValueAsString(secretConfig));
        when(configSplitter.splitConfig(any(), any(), anyInt(), eq(2))).thenReturn(List.of(configPart));

        source.writeConfig(config, false);

        verify(secretClient, times(1)).addSecretVersion(Mockito.eq("secret1"), keyVaultSecretArgumentCaptor.capture());
        verify(secretClient, times(1)).addSecretVersion(Mockito.eq("secret2"), keyVaultSecretArgumentCaptor.capture());

        List<SecretPayload> allValues = keyVaultSecretArgumentCaptor.getAllValues();
        Assertions.assertEquals(allValues.size(), 2);
        Assertions.assertEquals(objectMapper.readValue(allValues.get(0).getData().toStringUtf8(), Config.class), config);
        Assertions.assertEquals(objectMapper.readValue(allValues.get(1).getData().toStringUtf8(), Config.class), new Config());
    }

    @Test
    void writeConfig_shouldThrowExceptionNotEnoughSpace() throws JsonProcessingException {
        source = new GcpVaultConfigSource(secretClient, configSplitter, configMerger, List.of("secret1", "secret2"), objectMapper, "projectId");

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
        source = new GcpVaultConfigSource(secretClient, configSplitter, configMerger, List.of("secret1"), objectMapper, "projectId");

        Config config = new Config();
        CoreKey key = new CoreKey();
        key.setProject("project");
        key.setRole("role");
        config.setKeys(Map.of("key1", key));

        when(secretClient.accessSecretVersion(any(SecretVersionName.class)))
                .thenThrow(new NotFoundException(new RuntimeException(), HttpJsonStatusCode.of(StatusCode.Code.NOT_FOUND), false));

        source.writeConfig(config, true);

        verify(secretClient, times(1)).addSecretVersion(Mockito.eq("secret1"), keyVaultSecretArgumentCaptor.capture());

        List<SecretPayload> allValues = keyVaultSecretArgumentCaptor.getAllValues();
        Assertions.assertEquals(1, allValues.size());
        Assertions.assertEquals(config, objectMapper.readValue(allValues.get(0).getData().toStringUtf8(), Config.class));
    }

    @Test
    void writeConfig_shouldThrowExceptionWhenResourceNotFoundAndCreateResourcesFalse() {
        source = new GcpVaultConfigSource(secretClient, configSplitter, configMerger, List.of("secret1"), objectMapper, "projectId");

        Config config = new Config();
        CoreKey key = new CoreKey();
        key.setProject("project");
        key.setRole("role");
        config.setKeys(Map.of("key1", key));

        when(secretClient.accessSecretVersion(any(SecretVersionName.class)))
                .thenThrow(new NotFoundException(new RuntimeException(), HttpJsonStatusCode.of(StatusCode.Code.NOT_FOUND), false));

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