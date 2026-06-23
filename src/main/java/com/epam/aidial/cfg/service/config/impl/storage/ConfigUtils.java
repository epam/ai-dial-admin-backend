package com.epam.aidial.cfg.service.config.impl.storage;

import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreUpstream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ConfigUtils {

    public static Config secretsConfig(Config config) {
        Config secretConfig = new Config();
        secretConfig.setKeys(config.getKeys());
        secretConfig.setModels(config.getModels());
        secretConfig.setRoutes(config.getRoutes());
        secretConfig.setApplications(config.getApplications());
        secretConfig.setToolsets(config.getToolsets());
        return secretConfig;
    }


    public static void removeSecrets(Config config) {
        config.setKeys(Map.of());

        config.getModels().forEach((key, model) -> {
            model.setUpstreams(removeUpstreamsWithSecrets(model.getUpstreams()));
        });

        if (MapUtils.isNotEmpty(config.getRoutes())) {
            config.getRoutes().forEach((key, route) -> {
                route.setUpstreams(removeUpstreamsWithSecrets(route.getUpstreams()));
            });
        }

        if (MapUtils.isNotEmpty(config.getApplications())) {
            config.getApplications().forEach((key, application) -> {
                if (MapUtils.isNotEmpty(application.getRoutes())) {
                    application.getRoutes().forEach((routeKey, route) -> {
                        route.setUpstreams(removeUpstreamsWithSecrets(route.getUpstreams()));
                    });
                }
            });
        }

        config.getToolsets().forEach((key, toolset) -> {
            var authSettings = toolset.getAuthSettings();
            if (authSettings == null) {
                return;
            }
            toolset.getAuthSettings().setClientSecret(null);
        });
    }

    private static List<CoreUpstream> removeUpstreamsWithSecrets(List<CoreUpstream> coreUpstreamList) {
        if (CollectionUtils.isNotEmpty(coreUpstreamList)) {
            return coreUpstreamList.stream()
                    .filter(upstream -> (upstream.getKey() == null && upstream.getSecretExtraData() == null))
                    .collect(Collectors.toList());
        }
        return coreUpstreamList;
    }

    public static void removeEmptyCollections(Config config) {
        if (config.getAssistant() != null
                && MapUtils.isEmpty(config.getAssistant().getAssistants())) {
            config.getAssistant().setAssistants(null);
        }
        if (config.getAssistant() != null
                && MapUtils.isEmpty(config.getAssistant().getAssistants())
                && config.getAssistant().getEndpoint() == null
                && config.getAssistant().getFeatures() == null) {
            config.setAssistant(null);
        }
        if (MapUtils.isEmpty(config.getRoutes())) {
            config.setRoutes(null);
        }
        if (MapUtils.isEmpty(config.getModels())) {
            config.setModels(null);
        }
        if (MapUtils.isEmpty(config.getAddons())) {
            config.setAddons(null);
        }
        if (MapUtils.isEmpty(config.getApplications())) {
            config.setApplications(null);
        }
        if (MapUtils.isEmpty(config.getKeys())) {
            config.setKeys(null);
        }
        if (MapUtils.isEmpty(config.getRoles())) {
            config.setRoles(null);
        }
        if (MapUtils.isEmpty(config.getInterceptors())) {
            config.setInterceptors(null);
        }
        if (MapUtils.isEmpty(config.getApplicationTypeSchemas())) {
            config.setApplicationTypeSchemas(null);
        }
        if (CollectionUtils.isEmpty(config.getRetriableErrorCodes())) {
            config.setRetriableErrorCodes(null);
        }
        if (MapUtils.isEmpty(config.getToolsets())) {
            config.setToolsets(null);
        }
    }

}