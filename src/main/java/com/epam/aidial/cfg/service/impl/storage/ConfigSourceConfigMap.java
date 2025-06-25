package com.epam.aidial.cfg.service.impl.storage;

import com.epam.aidial.cfg.service.transfer.VersionAwareFieldFilter;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.util.HttpStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.fabric8.kubernetes.client.KubernetesClientException;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

@Slf4j
public class ConfigSourceConfigMap extends CompositeConfigSource {

    private final K8ConfigService k8ConfigService;
    private final String configKey;
    private final ObjectMapper objectMapper;

    public ConfigSourceConfigMap(VersionAwareFieldFilter versionAwareFieldFilter,
                                 ConfigSplitter configSplitter,
                                 ConfigMerger configMerger,
                                 List<String> secretNames,
                                 int maxSecretSize,
                                 K8ConfigService k8ConfigService,
                                 String configKey,
                                 ObjectMapper objectMapper) {
        super(versionAwareFieldFilter, configSplitter, configMerger, secretNames, maxSecretSize);
        this.k8ConfigService = k8ConfigService;
        this.configKey = configKey;
        this.objectMapper = objectMapper;
    }

    @Override
    protected String getSource(String sourceName) {
        return k8ConfigService.withClient(kubernetesClient
                        -> k8ConfigService.readConfigMapEntry(kubernetesClient, sourceName, configKey))
                .orElseThrow(() -> new IllegalStateException("ConfigMap is not found " + sourceName));
    }

    @Override
    protected void setSource(SourceValue source, boolean createResources) {
        try {
            k8ConfigService.withClient(kubernetesClient -> {
                k8ConfigService.updateConfigMapEntry(kubernetesClient, source.sourceName(), configKey, source.value());
                return null;
            });
        } catch (KubernetesClientException e) {
            if (e.getCode() == HttpStatus.NOT_FOUND.getCode()) {
                if (createResources) {
                    log.info("ConfigMap is not found, creating new one: {}", source.sourceName());
                    k8ConfigService.withClient(client -> {
                        k8ConfigService.createConfigMapEntry(client, source.sourceName(), configKey, source.value());
                        return null;
                    });
                } else {
                    log.info("ConfigMap is not found " + source.sourceName());
                    throw new IllegalStateException("ConfigMap is not found " + source.sourceName());
                }
            } else {
                throw e;
            }
        }
    }

    @Override
    @SneakyThrows
    protected Config decode(String encoded) {
        return objectMapper.readValue(encoded, Config.class);
    }

    @Override
    protected String encode(Config body) {
        try (var writer = new StringWriter()) {
            objectMapper.writeValue(writer, body);

            return writer.getBuffer().toString();

        } catch (IOException e) {
            throw new RuntimeException("Error writing config to config entry", e);
        }
    }
}
