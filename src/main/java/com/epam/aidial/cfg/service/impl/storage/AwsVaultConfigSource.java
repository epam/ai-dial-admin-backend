package com.epam.aidial.cfg.service.impl.storage;

import com.epam.aidial.cfg.service.transfer.VersionAwareFieldFilter;
import com.epam.aidial.core.config.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.ResourceNotFoundException;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class AwsVaultConfigSource extends CompositeConfigSource {

    private static final int MAX_SECRET_SIZE = 65536;
    private final ObjectMapper objectMapper;
    private final SecretsManagerClient secretsManagerClient;

    public AwsVaultConfigSource(VersionAwareFieldFilter versionAwareFieldFilter,
                                SecretsManagerClient secretsManagerClient,
                                ConfigSplitter configSplitter,
                                ConfigMerger configMerger,
                                List<String> secretNames,
                                ObjectMapper objectMapper) {
        super(versionAwareFieldFilter, configSplitter, configMerger, secretNames, MAX_SECRET_SIZE);
        this.objectMapper = objectMapper;
        this.secretsManagerClient = secretsManagerClient;
    }

    @Override
    protected String getSource(String sourceName) {
        return getSourceOptional(sourceName)
                .orElseThrow(() -> new IllegalStateException("Secret is not found " + sourceName));
    }

    private Optional<String> getSourceOptional(String sourceName) {
        GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                .secretId(sourceName)
                .build();
        try {
            GetSecretValueResponse valueResponse = secretsManagerClient.getSecretValue(valueRequest);
            return Optional.of(valueResponse.secretString());
        } catch (ResourceNotFoundException ex) {
            log.warn("Secret is not found %s", ex);
            return Optional.empty();
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

        PutSecretValueRequest putValueRequest = PutSecretValueRequest.builder()
                .secretId(source.sourceName())
                .secretString(source.value())
                .build();

        PutSecretValueResponse putValueResponse = secretsManagerClient.putSecretValue(putValueRequest);
        log.info("Secret updated successfully. Version: " + putValueResponse.versionId());
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
