package com.epam.aidial.cfg.configuration;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "azure.keyvault")
@Component
public class AzureKeyVaultProperties {
    @NonNull
    private String vaultUrl;
}