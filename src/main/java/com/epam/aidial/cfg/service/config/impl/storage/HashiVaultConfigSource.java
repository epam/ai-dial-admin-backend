package com.epam.aidial.cfg.service.config.impl.storage;

import com.epam.aidial.cfg.service.config.transfer.VersionAwareFieldFilter;
import com.epam.aidial.core.config.Config;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponse;
import org.springframework.vault.support.VaultResponseSupport;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public class HashiVaultConfigSource implements ConfigSource {

    private final VersionAwareFieldFilter versionAwareFieldFilter;
    private final VaultTemplate vaultTemplate;
    private final String secretPath;
    private final ObjectMapper objectMapper;

    @Override
    @SneakyThrows
    public Map<String, String> readRawConfig() {
        VaultResponse response = vaultTemplate.read(secretPath);
        Map<String, Object> data = response.getData();
        String content = objectMapper.writeValueAsString(data);
        return Map.of(secretPath, content);
    }

    @SneakyThrows
    @Override
    public Config readConfig() {
        Optional<JsonNode> config = readConfigOptional();
        return config.isPresent()
                ? objectMapper.treeToValue(config.get(), Config.class)
                : null;
    }

    private Optional<JsonNode> readConfigOptional() {
        VaultResponseSupport<SecretConfigVaultResponse> response = vaultTemplate.read(secretPath, SecretConfigVaultResponse.class);
        return Optional.ofNullable(response)
                .map(VaultResponseSupport::getData)
                .map(VaultResponseSupport::getData);
    }

    @Override
    public void writeConfig(Config configBody, boolean createResources) {
        Optional<JsonNode> persistedOptional = readConfigOptional();
        if (persistedOptional.isEmpty() && !createResources) {
            throw new IllegalStateException("Secret is not found " + secretPath);
        }

        JsonNode persisted = persistedOptional.orElse(null);

        Config secretConfig = ConfigUtils.secretsConfig(configBody);
        JsonNode versionedConfig = versionAwareFieldFilter.filterForTargetVersion(secretConfig);
        if (!Objects.equals(persisted, versionedConfig)) {
            vaultTemplate.write(secretPath, Map.of("data", versionedConfig));
        }
    }

    public static class SecretConfigVaultResponse extends VaultResponseSupport<JsonNode> {
    }
}
