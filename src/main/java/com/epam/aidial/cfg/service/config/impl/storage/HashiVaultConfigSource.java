package com.epam.aidial.cfg.service.config.impl.storage;

import com.epam.aidial.cfg.service.config.transfer.VersionAwareFieldFilter;
import com.epam.aidial.core.config.Config;
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

    @Override
    public Config readConfig() {
        return readConfigOptional().orElse(null);
    }

    private Optional<Config> readConfigOptional() {
        VaultResponseSupport<SecretConfigVaultResponse> response = vaultTemplate.read(secretPath, SecretConfigVaultResponse.class);
        return Optional.ofNullable(response)
                .map(VaultResponseSupport::getData)
                .map(VaultResponseSupport::getData);
    }

    @Override
    public void writeConfig(Config configBody, boolean createResources) {
        Config versionedConfig = versionAwareFieldFilter.filterForTargetVersion(configBody);

        Optional<Config> persistedOptional = readConfigOptional();
        if (persistedOptional.isEmpty() && !createResources) {
            throw new IllegalStateException("Secret is not found " + secretPath);
        }

        Config persisted = persistedOptional.orElse(null);
        Config secretConfig = ConfigUtils.secretsConfig(versionedConfig);
        if (!Objects.equals(persisted, secretConfig)) {
            vaultTemplate.write(secretPath, Map.of("data", secretConfig));
        }
    }

    public static class SecretConfigVaultResponse extends VaultResponseSupport<Config> {
    }
}
