package com.epam.aidial.cfg.configuration;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@Configuration
public class AwsVaultConfig {
    @Bean
    @ConditionalOnProperty(value = "config.export.keyvault.type", havingValue = "aws")
    public SecretsManagerClient secretsManagerClient() {
        SecretsManagerClient secretsClient = SecretsManagerClient.builder()
                .build();

        return secretsClient;
    }
}
