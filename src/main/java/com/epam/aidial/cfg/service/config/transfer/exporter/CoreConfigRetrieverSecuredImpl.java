package com.epam.aidial.cfg.service.config.transfer.exporter;

import com.epam.aidial.cfg.configuration.logging.LogExecution;
import com.epam.aidial.cfg.service.config.impl.storage.ConfigSource;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreApplication;
import com.epam.aidial.core.config.CoreModel;
import com.epam.aidial.core.config.CoreResourceAuthSettings;
import com.epam.aidial.core.config.CoreRoute;
import com.epam.aidial.core.config.CoreToolSet;
import com.epam.aidial.core.config.CoreUpstream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.MapUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@RequiredArgsConstructor
@LogExecution
public class CoreConfigRetrieverSecuredImpl implements CoreConfigRetriever {

    private final ConfigSource configSource;
    private final ConfigSource securedConfigSource;

    @Override
    public RawConfig getRawConfig(boolean addSecrets) {
        var rawConfigs = configSource.readRawConfig();
        Map<String, String> rawSecrets = null;
        if (addSecrets) {
            rawSecrets = securedConfigSource.readRawConfig();
        }
        return new RawConfig(rawConfigs, rawSecrets);
    }

    @Override
    public Config getConfig(boolean addSecrets) {
        Config publicConfig = Optional.of(configSource.readConfig())
                .orElseThrow(() -> new IllegalStateException("Can't read public core config from storage."));

        if (!addSecrets) {
            return publicConfig;
        }

        Config secretConfig = Optional.of(securedConfigSource.readConfig())
                .orElseThrow(() -> new IllegalStateException("Can't read secret core config from storage."));

        if (secretConfig.getKeys() != null) {
            publicConfig.setKeys(secretConfig.getKeys());
        }

        Map<String, CoreModel> models = getModels(optionalMap(publicConfig.getModels()), optionalMap(secretConfig.getModels()));
        publicConfig.setModels(models);

        LinkedHashMap<String, CoreRoute> routes = new LinkedHashMap<>(getRoutes(optionalMap(publicConfig.getRoutes()), optionalMap(secretConfig.getRoutes())));
        publicConfig.setRoutes(routes);

        Map<String, CoreApplication> applications = getApplications(optionalMap(publicConfig.getApplications()), optionalMap(secretConfig.getApplications()));
        publicConfig.setApplications(applications);

        Map<String, CoreToolSet> toolSets = getToolSets(optionalMap(publicConfig.getToolsets()), optionalMap(secretConfig.getToolsets()));
        publicConfig.setToolsets(toolSets);

        return publicConfig;
    }

    private Map<String, CoreModel> getModels(Map<String, CoreModel> publicModels, Map<String, CoreModel> secretModels) {
        for (Map.Entry<String, CoreModel> entry : secretModels.entrySet()) {
            String modelName = entry.getKey();
            CoreModel secretModel = entry.getValue();
            CoreModel publicModel = publicModels.computeIfAbsent(modelName, k -> new CoreModel());

            List<CoreUpstream> mergedUpstreams = new ArrayList<>();
            if (publicModel.getUpstreams() != null) {
                mergedUpstreams.addAll(publicModel.getUpstreams());
            }
            if (secretModel.getUpstreams() != null) {
                mergedUpstreams.addAll(secretModel.getUpstreams());
            }
            publicModel.setUpstreams(mergedUpstreams);
        }
        return publicModels;
    }

    private Map<String, CoreRoute> getRoutes(Map<String, CoreRoute> publicRoutes, Map<String, CoreRoute> secretRoutes) {
        for (Map.Entry<String, CoreRoute> entry : secretRoutes.entrySet()) {
            String routeName = entry.getKey();
            CoreRoute secretRoute = entry.getValue();
            CoreRoute publicRoute = publicRoutes.computeIfAbsent(routeName, k -> new CoreRoute());

            List<CoreUpstream> mergedUpstreams = new ArrayList<>();
            if (publicRoute.getUpstreams() != null) {
                mergedUpstreams.addAll(publicRoute.getUpstreams());
            }
            if (secretRoute.getUpstreams() != null) {
                mergedUpstreams.addAll(secretRoute.getUpstreams());
            }
            publicRoute.setUpstreams(mergedUpstreams);
        }
        return publicRoutes;
    }

    private Map<String, CoreApplication> getApplications(Map<String, CoreApplication> publicApps, Map<String, CoreApplication> secretApps) {
        for (Map.Entry<String, CoreApplication> entry : secretApps.entrySet()) {
            String appName = entry.getKey();
            CoreApplication secretApp = entry.getValue();
            CoreApplication publicApp = publicApps.computeIfAbsent(appName, k -> new CoreApplication());

            if (MapUtils.isNotEmpty(secretApp.getRoutes())) {
                LinkedHashMap<String, CoreRoute> publicRoutes = publicApp.getRoutes();
                if (publicRoutes == null) {
                    publicRoutes = new LinkedHashMap<>();
                    publicApp.setRoutes(publicRoutes);
                }

                for (Map.Entry<String, CoreRoute> routeEntry : secretApp.getRoutes().entrySet()) {
                    String routeName = routeEntry.getKey();
                    CoreRoute secretRoute = routeEntry.getValue();
                    CoreRoute publicRoute = publicRoutes.computeIfAbsent(routeName, k -> new CoreRoute());

                    List<CoreUpstream> mergedUpstreams = new ArrayList<>();
                    if (publicRoute.getUpstreams() != null) {
                        mergedUpstreams.addAll(publicRoute.getUpstreams());
                    }
                    if (secretRoute.getUpstreams() != null) {
                        mergedUpstreams.addAll(secretRoute.getUpstreams());
                    }
                    publicRoute.setUpstreams(mergedUpstreams);
                }
            }
        }
        return publicApps;
    }

    private Map<String, CoreToolSet> getToolSets(Map<String, CoreToolSet> publicToolSets, Map<String, CoreToolSet> secretToolSets) {
        for (Map.Entry<String, CoreToolSet> entry : secretToolSets.entrySet()) {
            String toolSetName = entry.getKey();
            CoreToolSet secretToolSet = entry.getValue();
            CoreToolSet publicToolSet = publicToolSets.computeIfAbsent(toolSetName, k -> new CoreToolSet());

            CoreResourceAuthSettings authSettings = publicToolSet.getAuthSettings();
            CoreResourceAuthSettings secretAuthSettings = secretToolSet.getAuthSettings();
            if (authSettings == null) {
                authSettings = Objects.requireNonNullElseGet(secretAuthSettings, CoreResourceAuthSettings::new);
            }
            if (secretAuthSettings != null) {
                authSettings.setClientSecret(secretAuthSettings.getClientSecret());
            }

            publicToolSet.setAuthSettings(authSettings);
        }
        return publicToolSets;
    }

    /* Helper that guarantees we always work on a mutable map. */
    private static <K, V> Map<K, V> optionalMap(Map<K, V> source) {
        return source == null ? new HashMap<>() :
                (source instanceof HashMap ? source : new HashMap<>(source));
    }

}