package com.epam.aidial.cfg.service.export;

import com.epam.aidial.cfg.service.impl.storage.ConfigSource;
import com.epam.aidial.cfg.service.impl.storage.ConfigUtils;
import com.epam.aidial.cfg.service.transfer.ConfigTransferLock;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreModel;
import com.epam.aidial.core.config.CoreResourceAuthSettings;
import com.epam.aidial.core.config.CoreToolSet;
import com.epam.aidial.core.config.CoreUpstream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class ConfigExportServiceSecuredImpl implements ConfigExportService {

    private final ConfigSource configSource;
    private final ConfigSource securedConfigSource;
    private final ConfigTransferLock configTransferLock;

    @Override
    public void export(Config config, boolean createResources) {
        Config secretConfig = secretConfig(config);
        ConfigUtils.removeSecrets(config);
        configTransferLock.withWriteLock(() -> {
            configSource.writeConfig(config, createResources);
            securedConfigSource.writeConfig(secretConfig, createResources);
        });
    }

    private Config secretConfig(Config config) {
        Config secretConfig = new Config();

        secretConfig.setRoles(Map.of());
        secretConfig.setKeys(config.getKeys());

        Map<String, CoreModel> models = config.getModels().entrySet()
                .stream()
                .map(e -> Pair.of(e.getKey(), mapModel(e.getValue())))
                .filter(p -> CollectionUtils.isNotEmpty(p.getValue().getUpstreams()))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        secretConfig.setModels(models);

        Map<String, CoreToolSet> toolsets = config.getToolsets().entrySet()
                .stream()
                .map(e -> Pair.of(e.getKey(), mapToolSet(e.getValue())))
                .collect(Collectors.toMap(Pair::getKey, Pair::getValue));
        secretConfig.setToolsets(toolsets);

        return secretConfig;
    }

    private CoreModel mapModel(CoreModel value) {
        List<CoreUpstream> upstreams = Optional.ofNullable(value)
                .map(CoreModel::getUpstreams)
                .stream()
                .flatMap(Collection::stream)
                .filter(upstream -> upstream.getKey() != null)
                .collect(Collectors.toList());

        CoreModel model = new CoreModel();
        model.setUpstreams(upstreams);
        return model;
    }

    private CoreToolSet mapToolSet(CoreToolSet value) {
        CoreResourceAuthSettings authSettings = new CoreResourceAuthSettings();
        authSettings.setClientSecret(StringUtils.EMPTY);

        if (value != null
                && value.getAuthSettings() != null
                && StringUtils.isNotEmpty(value.getAuthSettings().getClientSecret())) {
            authSettings.setClientSecret(value.getAuthSettings().getClientSecret());
        }

        CoreToolSet toolSet = new CoreToolSet();
        toolSet.setAuthSettings(authSettings);

        return toolSet;
    }

}
