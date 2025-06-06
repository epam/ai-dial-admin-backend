package com.epam.aidial.cfg.configuration;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.AzureCliCredential;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.identity.ManagedIdentityCredential;
import com.azure.identity.ManagedIdentityCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AzureAuthConfig {

    @Bean
    @ConditionalOnProperty(value = "azure.auth.type", havingValue = "managed")
    public ManagedIdentityCredential managedIdentityCredential() {
        return new ManagedIdentityCredentialBuilder().build();
    }

    @Bean
    @ConditionalOnProperty(value = "azure.auth.type", havingValue = "cli")
    public AzureCliCredential azureCliCredential(@Value("${azure.auth.tenantId}") String tenantId) {
        return new AzureCliCredentialBuilder()
                .tenantId(tenantId)
                .build();
    }

    @Bean
    @ConditionalOnProperty(value = "azure.auth.type", havingValue = "credential")
    public ClientSecretCredential clientSecretCredential(AzureCredentialProperties credentialProperties) {
        return new ClientSecretCredentialBuilder()
                .tenantId(credentialProperties.getTenantId())
                .clientId(credentialProperties.getClientId())
                .clientSecret(credentialProperties.getClientSecret())
                .build();
    }

    @Bean
    @ConditionalOnBean(TokenCredential.class)
    public SecretClient secretClient(TokenCredential credential, AzureKeyVaultProperties keyVaultProperties) {
        return new SecretClientBuilder()
                .vaultUrl(keyVaultProperties.getVaultUrl())
                .credential(credential)
                .buildClient();
    }
}
