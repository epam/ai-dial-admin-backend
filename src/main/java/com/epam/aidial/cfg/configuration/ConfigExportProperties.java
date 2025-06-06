package com.epam.aidial.cfg.configuration;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Data
@Configuration
@ConfigurationProperties(prefix = "config.export")
public class ConfigExportProperties {

    private ConfigExportStorageType storageType;

    private LocalFileStorage outputFile;
    private ConfigMapStorage configMap;
    private KeyVault keyvault;
    private String exportConfigFileName;
    private String exportConfigFileZipName;
    private String exportRawConfigFileZipName;

    @Data
    public static class LocalFileStorage {
        private String path;
    }

    @Data
    public static class ConfigMapStorage {
        private String name;
        private String config;
    }

    public enum ConfigExportStorageType {
        KUBE_SECRET,
        CONFIG_MAP,
        LOCAL_FILE
    }

    @Data
    public static class KeyVault {
        private KeyVaultType type;
        private List<String> secretNames;
        private String secretPath;
    }

    public enum KeyVaultType {
        none, azure, vault, aws, gcp
    }
}
