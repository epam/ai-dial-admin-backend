package com.epam.aidial.cfg.service.config.impl.storage;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import com.epam.aidial.cfg.service.config.transfer.VersionAwareFieldFilter;
import com.epam.aidial.core.config.Config;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class AzureKeyVaultConfigSource extends CompositeConfigSource {

    private static final int MAX_SECRET_SIZE = 25 * 1024;
    private final SecretClient secretClient;
    private final String expirationTimeUnit;
    private final long expirationPeriod;
    private final ObjectMapper objectMapper;

    public AzureKeyVaultConfigSource(VersionAwareFieldFilter versionAwareFieldFilter,
                                     ConfigSplitter configSplitter,
                                     ConfigMerger configMerger,
                                     List<String> secretNames,
                                     SecretClient secretClient,
                                     String expirationTimeUnit,
                                     long expirationPeriod,
                                     ObjectMapper objectMapper) {
        super(versionAwareFieldFilter, configSplitter, configMerger, secretNames, MAX_SECRET_SIZE);
        this.secretClient = secretClient;
        this.expirationTimeUnit = expirationTimeUnit;
        this.expirationPeriod = expirationPeriod;
        this.objectMapper = objectMapper;
    }

    @Override
    protected String getSource(String sourceName) {
        return getSourceOptional(sourceName)
                .orElseThrow(() -> new IllegalStateException("Secret is not found " + sourceName));
    }

    private Optional<String> getSourceOptional(String sourceName) {
        try {
            var secret = secretClient.getSecret(sourceName);
            return Optional.of(secret.getValue());
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
    protected Config decode(String encoded) {
        return objectMapper.readValue(encoded, Config.class);
    }

    @Override
    @SneakyThrows
    protected String encode(Config body) {
        return objectMapper.writeValueAsString(body);
    }
}
