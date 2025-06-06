package com.epam.aidial.cfg.service.impl.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
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

    public ConfigSourceKubeSecret(ConfigSplitter configSplitter,
                                  List<String> secretNames,
                                  int maxSecretSize,
                                  K8ConfigService k8ConfigService,
                                  String secretKey,
                                  ObjectMapper objectMapper) {
        super(configSplitter, secretNames, maxSecretSize);
        this.k8ConfigService = k8ConfigService;
        this.secretKey = secretKey;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void setSource(SourceValue source) {
        k8ConfigService.withClient(kubernetesClient -> {
            k8ConfigService.updateSecretMapEntry(kubernetesClient, source.sourceName(), secretKey,
                    source.value()
            );

            return null;
        });
    }

    @Override
    protected String encode(Object body) {
        try (var writer = new StringWriter()) {
            objectMapper.writeValue(writer, body);

            String jsonConfig = writer.getBuffer().toString();

            return Base64.getEncoder().encodeToString(jsonConfig.getBytes());
        } catch (IOException e) {
            throw new RuntimeException("Error writing config to config entry", e);
        }
    }
}
