package com.epam.aidial.cfg.service.impl.storage;

import com.epam.aidial.cfg.configuration.K8sProperties;
import com.epam.aidial.cfg.utils.SecretUtils;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.ConfigMapBuilder;
import io.fabric8.kubernetes.api.model.Secret;
import io.fabric8.kubernetes.api.model.SecretBuilder;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.DefaultKubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@Slf4j
@RequiredArgsConstructor
public class K8ConfigService {

    private final K8sProperties k8sProperties;

    public <T> T withClient(Function<KubernetesClient, T> task) {
        try (KubernetesClient client = createKubernetesClient(k8sProperties)) {
            return task.apply(client);
        }
    }

    public void createConfigMapEntry(KubernetesClient client, String resourceName, String key, String value) {
        log.trace("createConfigMapEntry. resourceName = {}. key = {}. value = {}", resourceName, key, value);
        final var configMap = new ConfigMapBuilder()
                .withNewMetadata()
                .withName(resourceName)
                .endMetadata()
                .withData(Map.of(key, value))
                .build();
        final var created = client.configMaps()
                .inNamespace(k8sProperties.getNamespace())
                .resource(configMap)
                .create();
        log.debug("createConfigMapEntry. Created ConfigMap {}, key {} = {}", created.getMetadata().getSelfLink(), key, value);
    }

    public void updateConfigMapEntry(KubernetesClient client, String resourceName, String key, String value) {
        log.trace("updateConfigMapEntry. resourceName = {}. key = {}. value = {}", resourceName, key, value);
        final var configMap = client.configMaps()
                .inNamespace(k8sProperties.getNamespace())
                .withName(resourceName)
                .edit(ConfigMapBuilder.class, builder -> builder.getData().replace(key, value));
        log.debug("updateConfigMapEntry. Updated ConfigMap {}, key {} = {}", configMap.getMetadata().getSelfLink(), key, value);
    }

    public Optional<String> readConfigMapEntry(KubernetesClient client, String resourceName, String key) {

        log.trace("readConfigMapEntry. resourceName={}, key={}", resourceName, key);
        String namespace = k8sProperties.getNamespace();
        ConfigMap configMap = client.configMaps()
                .inNamespace(namespace)
                .withName(resourceName)
                .get();
        log.debug("readConfigMapEntry. configMap: {} by namespace '{}', resourceName '{}'", configMap, namespace, resourceName);
        Optional<String> entry = Optional.ofNullable(configMap)
                .map(ConfigMap::getData)
                .map(stringStringMap -> stringStringMap.get(key));
        log.debug("readConfigMapEntry. config: {} by configName '{}'", entry, key);
        return entry;
    }

    private KubernetesClient createKubernetesClient(K8sProperties k8Config) {
        K8sProperties.K8ConnectType connectType = k8Config.getConnectType();
        K8sProperties.ClientConfig clientConfig = k8Config.getClient();
        ConfigBuilder configBuilder = new ConfigBuilder();
        configBuilder
                .withTrustCerts(k8Config.isTrustCerts())
                .withMaxConcurrentRequests(clientConfig.getMaxConcurrentRequests())
                .withMaxConcurrentRequestsPerHost(clientConfig.getMaxConcurrentRequestsPerHost())
                .withRequestRetryBackoffLimit(clientConfig.getRequestRetryBackoffLimit())
                .withRequestTimeout(clientConfig.getRequestTimeout())
                .withWebsocketPingInterval(clientConfig.getWithWebsocketPingInterval())
                .withWatchReconnectLimit(clientConfig.getWithWatchReconnectLimit());
        switch (connectType) {
            case CONFIG_FILE:
                return new DefaultKubernetesClient(configBuilder.build());
            case TOKEN:
                return new DefaultKubernetesClient(configBuilder
                        .withMasterUrl(k8Config.getMasterUrl())
                        .withOauthToken(new String(Base64.getDecoder().decode(k8Config.getOauthToken())))
                        .build());
            default:
                throw new IllegalStateException("Unexpected connectType value: " + connectType);
        }
    }

    public Optional<String> readSecretEntry(KubernetesClient client, String secretName, String secretKey) {
        log.trace("readSecretEntry. secretName={}, secretKey={}", secretName, secretKey);
        String namespace = k8sProperties.getNamespace();
        Secret secret = client.secrets()
                .inNamespace(namespace)
                .withName(secretName)
                .get();
        log.debug("readSecretEntry. secret: {} by namespace '{}', resourceName '{}'", secret, namespace, secretName);
        Optional<String> entry = Optional.ofNullable(secret)
                .map(Secret::getData)
                .map(stringStringMap -> stringStringMap.get(secretKey))
                .map(data -> new String(Base64.getDecoder().decode(data)));
        log.trace("readSecretEntry. secret: {} by configName '{}'", SecretUtils.mask(entry.orElse(null)), secretKey);
        return entry;
    }

    public void createSecretMapEntry(KubernetesClient client, String secretName, String secretKey, String value) {
        log.trace("createSecretMapEntry. secretName = {}. secretKey = {}. value = {}", secretName, secretKey, value);
        final var secret = new SecretBuilder()
                .withNewMetadata()
                .withName(secretName)
                .endMetadata()
                .withData(Map.of(secretKey, value))
                .build();
        final var created = client.secrets()
                .inNamespace(k8sProperties.getNamespace())
                .resource(secret)
                .create();
        log.debug("createSecretMapEntry. Created Secret {}, key {} = {}", created.getFullResourceName(), secretKey, SecretUtils.mask(value));
    }

    public void updateSecretMapEntry(KubernetesClient client, String secretName, String secretKey, String value) {
        log.trace("updateSecretMapEntry. secretName = {}. secretKey = {}. value = {}", secretName, secretKey, value);
        final var configMap = client.secrets()
                .inNamespace(k8sProperties.getNamespace())
                .withName(secretName)
                .edit(SecretBuilder.class, builder -> builder.getData().replace(secretKey, value));
        log.debug("updateSecretMapEntry. Updated Secret {}, key {} = {}", configMap.getFullResourceName(), secretKey, SecretUtils.mask(value));
    }
}

