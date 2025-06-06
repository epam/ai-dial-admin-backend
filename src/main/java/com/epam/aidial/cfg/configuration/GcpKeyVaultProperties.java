package com.epam.aidial.cfg.configuration;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@NoArgsConstructor
@ConfigurationProperties(prefix = "gcp.keyvault")
@Component
public class GcpKeyVaultProperties {
    private String projectId;
}