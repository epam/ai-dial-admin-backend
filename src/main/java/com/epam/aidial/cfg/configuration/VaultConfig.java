package com.epam.aidial.cfg.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.authentication.TokenAuthentication;
import org.springframework.vault.client.VaultEndpoint;
import org.springframework.vault.core.VaultTemplate;

import java.net.URI;

@Configuration
public class VaultConfig {

    @Bean
    @ConditionalOnProperty(value = {"vault.uri", "vault.token"})
    public VaultTemplate vaultTemplate(@Value("${vault.uri}") String uri,
                                       @Value("${vault.token}") String token) {
        URI vaultUri = URI.create(uri);
        VaultEndpoint endpoint = VaultEndpoint.from(vaultUri);
        TokenAuthentication tokenAuth = new TokenAuthentication(token);

        VaultTemplate vaultTemplate = new VaultTemplate(endpoint, tokenAuth);
        return vaultTemplate;
    }
}
