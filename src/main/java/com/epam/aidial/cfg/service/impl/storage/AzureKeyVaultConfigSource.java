package com.epam.aidial.cfg.service.impl.storage;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import com.epam.aidial.core.config.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Slf4j
public class AzureKeyVaultConfigSource extends CompositeConfigSource {

    private static final int MAX_SECRET_SIZE = 25 * 1024;
    private final SecretClient secretClient;
    private final String expirationTimeUnit;
    private final long expirationPeriod;
    private final ObjectMapper objectMapper;

    public AzureKeyVaultConfigSource(ConfigSplitter configSplitter,
                                     List<String> secretNames,
                                     SecretClient secretClient,
                                     String expirationTimeUnit,
                                     long expirationPeriod,
                                     ObjectMapper objectMapper) {
        super(configSplitter, secretNames, MAX_SECRET_SIZE);
        this.secretClient = secretClient;
        this.expirationTimeUnit = expirationTimeUnit;
        this.expirationPeriod = expirationPeriod;
        this.objectMapper = objectMapper;
    }

    @Override
    public Config readConfig() {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void setSource(SourceValue source) {
        String oldValue = secretClient.getSecret(source.sourceName()).getValue();
        if (Objects.equals(source.value(), oldValue)) {
            return;
        }
        KeyVaultSecret keyVaultSecret = new KeyVaultSecret(source.sourceName(), source.value());
        SecretProperties properties = new SecretProperties();
        properties.setEnabled(true);
        properties.setContentType("json");
        properties.setExpiresOn(OffsetDateTime.now().plus(expirationPeriod, ChronoUnit.valueOf(expirationTimeUnit)));
        keyVaultSecret.setProperties(properties);
        secretClient.setSecret(keyVaultSecret);
    }

    @Override
    @SneakyThrows
    protected String encode(Object body) {
        return objectMapper.writeValueAsString(body);
    }
}
