package com.epam.aidial.cfg.configuration;

import com.azure.security.keyvault.secrets.SecretClient;
import com.epam.aidial.cfg.service.config.export.ConfigExportService;
import com.epam.aidial.cfg.service.config.export.ConfigExportServiceImpl;
import com.epam.aidial.cfg.service.config.export.ConfigExportServiceSecuredImpl;
import com.epam.aidial.cfg.service.config.impl.storage.AwsVaultConfigSource;
import com.epam.aidial.cfg.service.config.impl.storage.AzureKeyVaultConfigSource;
import com.epam.aidial.cfg.service.config.impl.storage.ConfigMerger;
import com.epam.aidial.cfg.service.config.impl.storage.ConfigSource;
import com.epam.aidial.cfg.service.config.impl.storage.ConfigSourceConfigMap;
import com.epam.aidial.cfg.service.config.impl.storage.ConfigSourceFile;
import com.epam.aidial.cfg.service.config.impl.storage.ConfigSourceKubeSecret;
import com.epam.aidial.cfg.service.config.impl.storage.ConfigSplitter;
import com.epam.aidial.cfg.service.config.impl.storage.GcpVaultConfigSource;
import com.epam.aidial.cfg.service.config.impl.storage.HashiVaultConfigSource;
import com.epam.aidial.cfg.service.config.impl.storage.K8ConfigService;
import com.epam.aidial.cfg.service.config.transfer.ConfigTransferLock;
import com.epam.aidial.cfg.service.config.transfer.VersionAwareFieldFilter;
import com.epam.aidial.cfg.service.config.transfer.exporter.CoreConfigRetriever;
import com.epam.aidial.cfg.service.config.transfer.exporter.CoreConfigRetrieverImpl;
import com.epam.aidial.cfg.service.config.transfer.exporter.CoreConfigRetrieverSecuredImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.concurrent.BasicThreadFactory;
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
import java.util.concurrent.Executors;

@Configuration
@EnableScheduling
public class ExportConfiguration {

    @Bean("configSource")
    @Qualifier("configSource")
    @ConditionalOnProperty(value = "config.export.storageType", havingValue = "LOCAL_FILE")
    public ConfigSourceFile configSourceJsonFile(VersionAwareFieldFilter versionAwareFieldFilter,
                                                 ObjectMapper objectMapper,
                                                 @Value("${config.export.outputFile.path}") String outputFilePath) {
        return new ConfigSourceFile(versionAwareFieldFilter, objectMapper, outputFilePath);
    }

    @Bean("configSource")
    @Qualifier("configSource")
    @ConditionalOnProperty(value = "config.export.storageType", havingValue = "CONFIG_MAP")
    public ConfigSourceConfigMap configSourceJsonConfigMap(VersionAwareFieldFilter versionAwareFieldFilter,
                                                           ConfigSplitter configSplitter,
                                                           ConfigMerger configMerger,
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
        return new ConfigSourceConfigMap(versionAwareFieldFilter, configSplitter, configMerger, names, maxSize, k8ConfigService, configKey, objectMapper);
    }

    @Bean("configSource")
    @Qualifier("configSource")
    @ConditionalOnProperty(value = "config.export.storageType", havingValue = "KUBE_SECRET")
    public ConfigSourceKubeSecret configSourceJsonKubeSecret(VersionAwareFieldFilter versionAwareFieldFilter,
                                                             ConfigSplitter configSplitter,
                                                             ConfigMerger configMerger,
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
        return new ConfigSourceKubeSecret(versionAwareFieldFilter, configSplitter, configMerger, names, maxSize, k8ConfigService, kubeSecretKey, objectMapper);
    }

    @Bean
    @ConditionalOnExpression(("'${config.export.storageType}' == 'CONFIG_MAP' OR '${config.export.storageType}' == 'KUBE_SECRET'"))
    public K8ConfigService k8ConfigService(K8sProperties k8sProperties) {
        BasicThreadFactory factory = BasicThreadFactory.builder()
                .namingPattern("k8-client-pool-%d")
                .build();
        return new K8ConfigService(k8sProperties, Executors.newCachedThreadPool(factory));
    }

    @Bean
    @ConditionalOnProperty(value = "config.export.keyvault.type", havingValue = "none", matchIfMissing = true)
    public ConfigExportService configExportService(ConfigSource configSource, ConfigTransferLock lock) {
        return new ConfigExportServiceImpl(configSource, lock);
    }

    @Bean
    @ConditionalOnExpression("'${config.export.keyvault.type:none}' != 'none'")
    public ConfigExportService securedConfigExportService(@Qualifier("configSource") ConfigSource configSource,
                                                          @Qualifier("securedConfigSource") ConfigSource securedConfigSource,
                                                          ConfigTransferLock lock) {
        return new ConfigExportServiceSecuredImpl(configSource, securedConfigSource, lock);
    }

    @Bean
    @ConditionalOnProperty(value = "config.export.keyvault.type", havingValue = "none", matchIfMissing = true)
    public CoreConfigRetriever coreConfigRetriever(ConfigSource configSource, ConfigTransferLock lock) {
        return new CoreConfigRetrieverImpl(configSource, lock);
    }

    @Bean
    @ConditionalOnExpression("'${config.export.keyvault.type:none}' != 'none'")
    public CoreConfigRetriever securedCoreConfigRetriever(@Qualifier("configSource") ConfigSource configSource,
                                                          @Qualifier("securedConfigSource") ConfigSource securedConfigSource) {
        return new CoreConfigRetrieverSecuredImpl(configSource, securedConfigSource);
    }

    @Bean("securedConfigSource")
    @Qualifier("securedConfigSource")
    @ConditionalOnProperty(value = "config.export.keyvault.type", havingValue = "azure")
    public AzureKeyVaultConfigSource azureKeyVaultConfigSource(VersionAwareFieldFilter versionAwareFieldFilter,
                                                               ObjectMapper objectMapper,
                                                               SecretClient secretClient,
                                                               @Value("${config.export.keyvault.secretNames}") List<String> secretNames,
                                                               @Value("${config.export.keyvault.expiration.unit}") String expirationTimeUnit,
                                                               @Value("${config.export.keyvault.expiration.period}") Long expirationPeriod,
                                                               ConfigSplitter configSplitter,
                                                               ConfigMerger configMerger) {
        return new AzureKeyVaultConfigSource(versionAwareFieldFilter, configSplitter, configMerger, secretNames, secretClient, expirationTimeUnit, expirationPeriod, objectMapper);
    }

    @Bean("securedConfigSource")
    @Qualifier("securedConfigSource")
    @ConditionalOnProperty(value = "config.export.keyvault.type", havingValue = "vault")
    public HashiVaultConfigSource hashiVaultConfigSource(VersionAwareFieldFilter versionAwareFieldFilter,
                                                         ObjectMapper objectMapper,
                                                         @Value("${config.export.keyvault.secretPath}") String secretPath,
                                                         VaultTemplate vaultTemplate) {
        return new HashiVaultConfigSource(versionAwareFieldFilter, vaultTemplate, secretPath, objectMapper);
    }

    @Bean("securedConfigSource")
    @Qualifier("securedConfigSource")
    @ConditionalOnProperty(value = "config.export.keyvault.type", havingValue = "aws")
    public AwsVaultConfigSource awsVaultConfigSource(VersionAwareFieldFilter versionAwareFieldFilter,
                                                     ObjectMapper objectMapper,
                                                     SecretsManagerClient secretsManagerClient,
                                                     @Value("${config.export.keyvault.secretNames}") List<String> secretNames,
                                                     @Value("${config.export.keyvault.expiration.unit}") String expirationTimeUnit,
                                                     @Value("${config.export.keyvault.expiration.period}") Long expirationPeriod,
                                                     ConfigSplitter configSplitter,
                                                     ConfigMerger configMerger) {
        return new AwsVaultConfigSource(versionAwareFieldFilter, secretsManagerClient, configSplitter, configMerger, secretNames, objectMapper);
    }

    @Bean("securedConfigSource")
    @Qualifier("securedConfigSource")
    @ConditionalOnProperty(value = "config.export.keyvault.type", havingValue = "gcp")
    public GcpVaultConfigSource gcpVaultConfigSource(VersionAwareFieldFilter versionAwareFieldFilter,
                                                     ObjectMapper objectMapper,
                                                     SecretManagerServiceClient secretsManagerClient,
                                                     @Value("${config.export.keyvault.secretNames}") List<String> secretNames,
                                                     @Value("${gcp.keyvault.projectId}") String projectId,
                                                     ConfigSplitter configSplitter,
                                                     ConfigMerger configMerger) {
        return new GcpVaultConfigSource(versionAwareFieldFilter, secretsManagerClient, configSplitter, configMerger, secretNames, objectMapper, projectId);
    }

    private List<String> resolveNames(String name, List<String> names) {
        if (StringUtils.isNotEmpty(name)) {
            return List.of(name);
        }
        return names;
    }
}


