package com.epam.aidial.cfg.service.impl.storage;

import com.epam.aidial.core.config.Config;
import lombok.RequiredArgsConstructor;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultResponseSupport;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
public class HashiVaultConfigSource implements ConfigSource {

    private final VaultTemplate vaultTemplate;
    private final String secretPath;

    @Override
    public Config readConfig() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeConfig(Config configBody) {
        Config secretConfig = ConfigUtils.secretsConfig(configBody);
        VaultResponseSupport<SecretConfigVaultResponse> response = vaultTemplate.read(secretPath, SecretConfigVaultResponse.class);
        Config persisted = Optional.ofNullable(response)
                .map(VaultResponseSupport::getData)
                .map(VaultResponseSupport::getData)
                .orElse(null);
        if (!Objects.equals(persisted, secretConfig)) {
            vaultTemplate.write(secretPath, Map.of("data", secretConfig));
        }
    }

    public static class SecretConfigVaultResponse extends VaultResponseSupport<Config> {
    }
}
