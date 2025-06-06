package com.epam.aidial.cfg.service.impl.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.protobuf.ByteString;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;

@Slf4j
public class GcpVaultConfigSource extends CompositeConfigSource {

    private static final int MAX_SECRET_SIZE = 65536;
    public static final String LATEST = "latest";
    private final ObjectMapper objectMapper;
    private final SecretManagerServiceClient secretManagerServiceClient;
    private final String projectId;

    public GcpVaultConfigSource(SecretManagerServiceClient secretManagerServiceClient,
                                ConfigSplitter configSplitter,
                                List<String> secretNames,
                                ObjectMapper objectMapper, String projectId) {
        super(configSplitter, secretNames, MAX_SECRET_SIZE);
        this.objectMapper = objectMapper;
        this.secretManagerServiceClient = secretManagerServiceClient;
        this.projectId = projectId;
    }

    @Override
    protected void setSource(SourceValue source) {

        SecretVersionName secretVersionName = SecretVersionName.of(projectId, source.sourceName(), LATEST);
        AccessSecretVersionResponse response = secretManagerServiceClient.accessSecretVersion(secretVersionName);
        String oldValue = response.getPayload().getData().toStringUtf8();

        if (Objects.equals(source.value(), oldValue)) {
            return;
        }

        SecretPayload secretPayload = SecretPayload.newBuilder()
                .setData(ByteString.copyFromUtf8(source.value()))
                .build();

        secretManagerServiceClient.addSecretVersion(source.sourceName(), secretPayload);
    }

    @Override
    @SneakyThrows
    protected String encode(Object body) {
        return objectMapper.writeValueAsString(body);
    }
}
