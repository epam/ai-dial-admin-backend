package com.epam.aidial.cfg.service.impl.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueRequest;
import software.amazon.awssdk.services.secretsmanager.model.PutSecretValueResponse;

import java.util.List;
import java.util.Objects;

@Slf4j
public class AwsVaultConfigSource extends CompositeConfigSource {

    private static final int MAX_SECRET_SIZE = 65536;
    private final ObjectMapper objectMapper;
    private final SecretsManagerClient secretsManagerClient;

    public AwsVaultConfigSource(SecretsManagerClient secretsManagerClient,
                                ConfigSplitter configSplitter,
                                List<String> secretNames,
                                ObjectMapper objectMapper) {
        super(configSplitter, secretNames, MAX_SECRET_SIZE);
        this.objectMapper = objectMapper;
        this.secretsManagerClient = secretsManagerClient;
    }

    @Override
    protected void setSource(SourceValue source) {

        GetSecretValueRequest valueRequest = GetSecretValueRequest.builder()
                .secretId(source.sourceName())
                .build();
        GetSecretValueResponse valueResponse = secretsManagerClient.getSecretValue(valueRequest);
        String oldValue = valueResponse.secretString();

        if (Objects.equals(source.value(), oldValue)) {
            return;
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
    protected String encode(Object body) {
        return objectMapper.writeValueAsString(body);
    }
}
