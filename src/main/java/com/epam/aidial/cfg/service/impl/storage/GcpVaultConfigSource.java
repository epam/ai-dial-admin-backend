package com.epam.aidial.cfg.service.impl.storage;

import com.epam.aidial.cfg.service.transfer.VersionAwareFieldFilter;
import com.epam.aidial.core.config.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.gax.rpc.ApiException;
import com.google.api.gax.rpc.StatusCode;
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import com.google.cloud.secretmanager.v1.SecretPayload;
import com.google.cloud.secretmanager.v1.SecretVersionName;
import com.google.protobuf.ByteString;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class GcpVaultConfigSource extends CompositeConfigSource {

    private static final int MAX_SECRET_SIZE = 65536;
    public static final String LATEST = "latest";
    private final ObjectMapper objectMapper;
    private final SecretManagerServiceClient secretManagerServiceClient;
    private final String projectId;

    public GcpVaultConfigSource(VersionAwareFieldFilter versionAwareFieldFilter,
                                SecretManagerServiceClient secretManagerServiceClient,
                                ConfigSplitter configSplitter,
                                ConfigMerger configMerger,
                                List<String> secretNames,
                                ObjectMapper objectMapper, String projectId) {
        super(versionAwareFieldFilter, configSplitter, configMerger, secretNames, MAX_SECRET_SIZE);
        this.objectMapper = objectMapper;
        this.secretManagerServiceClient = secretManagerServiceClient;
        this.projectId = projectId;
    }

    @Override
    protected String getSource(String sourceName) {
        SecretVersionName secretVersionName = SecretVersionName.of(projectId, sourceName, LATEST);
        AccessSecretVersionResponse response = secretManagerServiceClient.accessSecretVersion(secretVersionName);
        return response.getPayload().getData().toStringUtf8();
    }

    private Optional<String> getSourceOptional(String sourceName) {
        try {
            SecretVersionName secretVersionName = SecretVersionName.of(projectId, sourceName, LATEST);
            AccessSecretVersionResponse response = secretManagerServiceClient.accessSecretVersion(secretVersionName);
            return Optional.of(response.getPayload().getData().toStringUtf8());
        } catch (ApiException ex) {
            if (ex.getStatusCode().getCode() == StatusCode.Code.NOT_FOUND) {
                log.warn("Secret is not found %s", ex);
                return Optional.empty();
            } else {
                throw ex;
            }
        }
    }

    @Override
    protected void setSource(SourceValue source, boolean createResources) {
        Optional<String> oldValue = getSourceOptional(source.sourceName());
        if (oldValue.isPresent() && Objects.equals(source.value(), oldValue.get())) {
            return;
        }
        if (oldValue.isEmpty() && !createResources) {
            throw new IllegalStateException("Secret is not found " + source.sourceName());
        }

        SecretPayload secretPayload = SecretPayload.newBuilder()
                .setData(ByteString.copyFromUtf8(source.value()))
                .build();

        secretManagerServiceClient.addSecretVersion(source.sourceName(), secretPayload);
    }

    @Override
    @SneakyThrows
    protected Config decode(String encoded) {
        return objectMapper.readValue(encoded, Config.class);
    }

    @Override
    @SneakyThrows
    protected String encode(Config body) {
        return objectMapper.writeValueAsString(body);
    }
}
