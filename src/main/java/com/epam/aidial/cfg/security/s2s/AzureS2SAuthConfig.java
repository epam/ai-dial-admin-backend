package com.epam.aidial.cfg.security.s2s;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.validation.annotation.Validated;

@Configuration
public class AzureS2SAuthConfig {

    @Bean
    @Validated
    @Profile(DialAdminProfile.LOCAL)
    public AzureClientSecretProperties clientSecretProperties() {
        return new AzureClientSecretProperties();
    }

    @Bean
    @Profile(DialAdminProfile.LOCAL)
    public TokenCredential clientSecretTokenCredential(final AzureClientSecretProperties props) {
        return new ClientSecretCredentialBuilder()
            .tenantId(props.getTenantId())
            .clientId(props.getClientId())
            .clientSecret(props.getClientSecret())
            .build();
    }

    @Bean
    @Profile({DialAdminProfile.AZURE, DialAdminProfile.DEFAULT})
    public TokenCredential msiTokenCredential() {
        return new ManagedIdentityCredentialBuilder()
            .build();
    }
}
