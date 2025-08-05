package com.epam.aidial.cfg.security.s2s;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

@Data
public class AzureClientSecretProperties {

    @Value("${azure.auth.tenantId}")
    @NotEmpty
    private String tenantId;

    @Value("${azure.auth.clientId}")
    @NotEmpty
    private String clientId;

    @Value("${azure.auth.clientSecret}")
    @NotEmpty
    private String clientSecret;
}
