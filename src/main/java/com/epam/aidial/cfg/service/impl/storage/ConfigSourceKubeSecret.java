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
import java.util.Base64;
import java.util.List;

@Slf4j
public class ConfigSourceKubeSecret extends CompositeConfigSource {
    private final K8ConfigService k8ConfigService;
    private final String secretKey;
    private final ObjectMapper objectMapper;

    public ConfigSourceKubeSecret(VersionAwareFieldFilter versionAwareFieldFilter,
                                  ConfigSplitter configSplitter,
                                  ConfigMerger configMerger,
                                  List<String> secretNames,
                                  int maxSecretSize,
                                  K8ConfigService k8ConfigService,
                                  String secretKey,
                                  ObjectMapper objectMapper) {
        super(versionAwareFieldFilter, configSplitter, configMerger, secretNames, maxSecretSize);
        this.k8ConfigService = k8ConfigService;
        this.secretKey = secretKey;
        this.objectMapper = objectMapper;
    }

    @Override
    protected String getSource(String sourceName) {
        return k8ConfigService.withClient(kubernetesClient
                        -> k8ConfigService.readSecretEntry(kubernetesClient, sourceName, secretKey))
                .orElseThrow(() -> new IllegalStateException("Secret is not found " + sourceName));
    }

    @Override
    protected void setSource(SourceValue source, boolean createResources) {
        try {
            k8ConfigService.withClient(kubernetesClient -> {
                k8ConfigService.updateSecretMapEntry(kubernetesClient, source.sourceName(), secretKey, source.value());
                return null;
            });
        } catch (KubernetesClientException e) {
            if (e.getCode() == HttpStatus.NOT_FOUND.getCode()) {
                if (createResources) {
                    log.info("Secret is not found, creating new one: {}", source.sourceName());
                    k8ConfigService.withClient(client -> {
                        k8ConfigService.createSecretMapEntry(client, source.sourceName(), secretKey, source.value());
                        return null;
                    });
                } else {
                    log.info("Secret is not found " + source.sourceName());
                    throw new IllegalStateException("Secret is not found " + source.sourceName());
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

            String jsonConfig = writer.getBuffer().toString();

            return Base64.getEncoder().encodeToString(jsonConfig.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error writing config to config entry", e);
        }
    }
}
