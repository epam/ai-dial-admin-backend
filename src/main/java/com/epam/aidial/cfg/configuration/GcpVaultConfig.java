package com.epam.aidial.cfg.configuration;

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import lombok.SneakyThrows;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GcpVaultConfig {
    @Bean
    @ConditionalOnProperty(value = "config.export.keyvault.type", havingValue = "gcp")
    @SneakyThrows
    public SecretManagerServiceClient secretManagerServiceClient() {
        return SecretManagerServiceClient.create();
    }
}
