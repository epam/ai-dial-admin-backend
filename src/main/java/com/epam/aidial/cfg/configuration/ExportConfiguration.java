package com.epam.aidial.cfg.configuration;

import com.azure.security.keyvault.secrets.SecretClient;
import com.epam.aidial.cfg.service.export.ConfigExportService;
import com.epam.aidial.cfg.service.export.ConfigExportServiceImpl;
import com.epam.aidial.cfg.service.export.ConfigExportServiceSecuredImpl;
import com.epam.aidial.cfg.service.impl.storage.AwsVaultConfigSource;
import com.epam.aidial.cfg.service.impl.storage.AzureKeyVaultConfigSource;
import com.epam.aidial.cfg.service.impl.storage.ConfigSource;
import com.epam.aidial.cfg.service.impl.storage.ConfigSourceConfigMap;
import com.epam.aidial.cfg.service.impl.storage.ConfigSourceFile;
import com.epam.aidial.cfg.service.impl.storage.ConfigSourceKubeSecret;
import com.epam.aidial.cfg.service.impl.storage.ConfigSplitter;
import com.epam.aidial.cfg.service.impl.storage.GcpVaultConfigSource;
import com.epam.aidial.cfg.service.impl.storage.HashiVaultConfigSource;
import com.epam.aidial.cfg.service.impl.storage.K8ConfigService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.vault.core.VaultTemplate;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

import java.util.List;

@Configuration
@ConditionalOnProperty(value = "config.export.enabled", havingValue = "true")
@EnableScheduling
public class ExportConfiguration {

    @Bean("configSource")
    @Qualifier("configSource")
    @ConditionalOnProperty(value = "config.export.storageType", havingValue = "LOCAL_FILE")
    public ConfigSourceFile configSourceJsonFile(ObjectMapper objectMapper, @Value("${config.export.outputFile.path}") String outputFilePath) {
        return new ConfigSourceFile(objectMapper, outputFilePath);
    }

    @Bean("configSource")
    @Qualifier("configSource")
    @ConditionalOnProperty(value = "config.export.storageType", havingValue = "CONFIG_MAP")
    public ConfigSourceConfigMap configSourceJsonConfigMap(ConfigSplitter configSplitter,
                                                           @Value("${config.export.configMap.maxSize:1048576}") int maxSize,
                                                           @Value("${config.export.configMap.name:}") String configMapName,
                                                           @Value("${config.export.configMap.names:}") List<String> configMapNames,
                                                           @Value("${config.export.configMap.key}") String configKey,
                                                           K8ConfigService k8ConfigService,
                                                           ObjectMapper objectMapper) {
        if (StringUtils.isNotEmpty(configMapName) && CollectionUtils.isNotEmpty(configMapNames)) {
            throw new IllegalArgumentException("Config map name and names are both specified");
        }
        List<String> names = resolveNames(configMapName, configMapNames);
        return new ConfigSourceConfigMap(configSplitter, names, maxSize, k8ConfigService, configKey, objectMapper);
    }

    @Bean("configSource")
    @Qualifier("configSource")
    @ConditionalOnProperty(value = "config.export.storageType", havingValue = "KUBE_SECRET")
    public ConfigSourceKubeSecret configSourceJsonKubeSecret(ConfigSplitter configSplitter,
                                                             @Value("${config.export.kubeSecret.maxSize:1048576}") int maxSize,
                                                             @Value("${config.export.kubeSecret.name:}") String kubeSecretName,
                                                             @Value("${config.export.kubeSecret.names:}") List<String> kubeSecretNames,
                                                             @Value("${config.export.kubeSecret.key}") String kubeSecretKey,
                                                             K8ConfigService k8ConfigService,
                                                             ObjectMapper objectMapper) {
        if (StringUtils.isNotEmpty(kubeSecretName) && CollectionUtils.isNotEmpty(kubeSecretNames)) {
            throw new IllegalArgumentException("Kube secret name and names are both specified");
        }
        List<String> names = resolveNames(kubeSecretName, kubeSecretNames);
        return new ConfigSourceKubeSecret(configSplitter, names, maxSize, k8ConfigService, kubeSecretKey, objectMapper);
    }

    @Bean
    @ConditionalOnExpression(("'${config.export.storageType}' == 'CONFIG_MAP' OR '${config.export.storageType}' == 'KUBE_SECRET'"))
    public K8ConfigService k8ConfigService(K8sProperties k8sProperties) {
        return new K8ConfigService(k8sProperties);
    }

    @Bean
    @ConditionalOnProperty(value = "config.export.keyvault.type", havingValue = "none", matchIfMissing = true)
    public ConfigExportService configExportService(ConfigSource configSource) {
        return new ConfigExportServiceImpl(configSource);
    }

    @Bean
    @ConditionalOnExpression("'${config.export.keyvault.type:none}' != 'none'")
    public ConfigExportService securedConfigExportService(@Qualifier("configSource") ConfigSource configSource,
                                                          @Qualifier("securedConfigSource") ConfigSource securedConfigSource) {
        return new ConfigExportServiceSecuredImpl(configSource, securedConfigSource);
    }

    @Bean("securedConfigSource")
    @Qualifier("securedConfigSource")
    @ConditionalOnProperty(value = "config.export.keyvault.type", havingValue = "azure")
    public AzureKeyVaultConfigSource azureKeyVaultConfigSource(ObjectMapper objectMapper,
                                                               SecretClient secretClient,
                                                               @Value("${config.export.keyvault.secretNames}") List<String> secretNames,
                                                               @Value("${config.export.keyvault.expiration.unit}") String expirationTimeUnit,
                                                               @Value("${config.export.keyvault.expiration.period}") Long expirationPeriod,
                                                               ConfigSplitter configSplitter) {
        return new AzureKeyVaultConfigSource(configSplitter, secretNames, secretClient, expirationTimeUnit, expirationPeriod, objectMapper);
    }

    @Bean("securedConfigSource")
    @Qualifier("securedConfigSource")
    @ConditionalOnProperty(value = "config.export.keyvault.type", havingValue = "vault")
    public HashiVaultConfigSource hashiVaultConfigSource(@Value("${config.export.keyvault.secretPath}") String secretPath,
                                                         VaultTemplate vaultTemplate) {
        return new HashiVaultConfigSource(vaultTemplate, secretPath);
    }

    @Bean("securedConfigSource")
    @Qualifier("securedConfigSource")
    @ConditionalOnProperty(value = "config.export.keyvault.type", havingValue = "aws")
    public AwsVaultConfigSource awsVaultConfigSource(ObjectMapper objectMapper,
                                                     SecretsManagerClient secretsManagerClient,
                                                     @Value("${config.export.keyvault.secretNames}") List<String> secretNames,
                                                     @Value("${config.export.keyvault.expiration.unit}") String expirationTimeUnit,
                                                     @Value("${config.export.keyvault.expiration.period}") Long expirationPeriod,
                                                     ConfigSplitter configSplitter) {
        return new AwsVaultConfigSource(secretsManagerClient, configSplitter, secretNames, objectMapper);
    }

    @Bean("securedConfigSource")
    @Qualifier("securedConfigSource")
    @ConditionalOnProperty(value = "config.export.keyvault.type", havingValue = "gcp")
    public GcpVaultConfigSource gcpVaultConfigSource(ObjectMapper objectMapper,
                                                     SecretManagerServiceClient secretsManagerClient,
                                                     @Value("${config.export.keyvault.secretNames}") List<String> secretNames,
                                                     @Value("${gcp.keyvault.projectId}") String projectId,
                                                     ConfigSplitter configSplitter) {
        return new GcpVaultConfigSource(secretsManagerClient, configSplitter, secretNames, objectMapper, projectId);
    }

    private List<String> resolveNames(String name, List<String> names) {
        if (StringUtils.isNotEmpty(name)) {
            return List.of(name);
        }
        return names;
    }
}


