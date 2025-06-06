package com.epam.aidial.cfg.service.impl.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;

@Slf4j
public class ConfigSourceConfigMap extends CompositeConfigSource {

    private final K8ConfigService k8ConfigService;
    private final String configKey;
    private final ObjectMapper objectMapper;

    public ConfigSourceConfigMap(ConfigSplitter configSplitter,
                                 List<String> secretNames,
                                 int maxSecretSize,
                                 K8ConfigService k8ConfigService,
                                 String configKey,
                                 ObjectMapper objectMapper) {
        super(configSplitter, secretNames, maxSecretSize);
        this.k8ConfigService = k8ConfigService;
        this.configKey = configKey;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void setSource(SourceValue source) {
        k8ConfigService.withClient(kubernetesClient -> {
            k8ConfigService.updateConfigMapEntry(kubernetesClient, source.sourceName(), configKey,
                    source.value()
            );

            return null;
        });
    }

    @Override
    protected String encode(Object body) {
        try (var writer = new StringWriter()) {
            objectMapper.writeValue(writer, body);

            return writer.getBuffer().toString();

        } catch (IOException e) {
            throw new RuntimeException("Error writing config to config entry", e);
        }
    }
}
