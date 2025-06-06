package com.epam.aidial.cfg.service.export;

import com.epam.aidial.cfg.service.impl.storage.ConfigSource;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreModel;
import com.epam.aidial.core.config.CoreUpstream;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
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

    @Override
    public void export(Config config) {
        Config secretConfig = secretConfig(config);
        removeSecrets(config);
        configSource.writeConfig(config);
        securedConfigSource.writeConfig(secretConfig);
    }

    private void removeSecrets(Config config) {
        config.setKeys(Map.of());
        config.getModels().forEach((key, model) -> {
            if (model.getUpstreams() == null) {
                return;
            }
            List<CoreUpstream> upstreams = model.getUpstreams()
                    .stream()
                    .filter(upstream -> upstream.getKey() == null)
                    .collect(Collectors.toList());
            model.setUpstreams(upstreams);
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

}
