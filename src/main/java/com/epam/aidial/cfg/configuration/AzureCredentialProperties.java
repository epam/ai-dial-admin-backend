package com.epam.aidial.cfg.configuration;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "azure.auth")
@Component
public class AzureCredentialProperties {

    @NonNull
    private String type;
    @NonNull
    private String tenantId;
    @NonNull
    private String clientId;
    @NonNull
    private String clientSecret;
}