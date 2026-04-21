package com.epam.aidial.cfg.service.config.impl.storage;

import com.epam.aidial.core.config.Config;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
public abstract class CompositeConfigSource implements ConfigSource {

    private final ConfigSplitter configSplitter;
    private final ConfigMerger configMerger;
    private final List<String> sourceNames;
    private final int maxSourceSize;

    @Override
    public Map<String, String> readRawConfig() {
        if (CollectionUtils.isEmpty(sourceNames)) {
            throw new IllegalStateException("Unable to read configuration, names is not configured.");
        }

        return sourceNames.stream()
                .collect(Collectors.toMap(sourceName -> sourceName, this::getSource));
    }

    @Override
    @SneakyThrows
    public Config readConfig() {
        if (CollectionUtils.isEmpty(sourceNames)) {
            throw new IllegalStateException("Unable to read configuration, names is not configured.");
        }

        var encodedParts = sourceNames.stream()
                .map(this::getSource)
                .toList();

        return configMerger.merge(encodedParts, this::decode);
    }

    @Override
    public void writeConfig(Config configBody, boolean createResources) {
        List<SourceValue> sourceValues = resolveSourceNames(configBody);

        for (SourceValue sourceValue : sourceValues) {
            setSource(sourceValue, createResources);
        }
    }

    @SneakyThrows
    protected List<SourceValue> resolveSourceNames(Config configBody) {
        if (CollectionUtils.isEmpty(sourceNames)) {
            throw new IllegalStateException("Unable to store source, names is not configured.");
        }
        if (sourceNames.size() == 1) {
            String newValue = encode(configBody);
            return List.of(new SourceValue(sourceNames.get(0), newValue));
        }

        List<ConfigPart> configs = configSplitter.splitConfig(configBody, this::encode, maxSourceSize, sourceNames.size());

        if (configs.size() > sourceNames.size()) {
            log.info("configs size {}, names size {}", configs.size(), sourceNames.size());
            throw new IllegalStateException("Unable to persist sources, there is not enough space");
        }

        List<SourceValue> sourceValues = new ArrayList<>();
        for (int i = 0; i < sourceNames.size(); i++) {
            String sourceName = sourceNames.get(i);
            ConfigPart configPart = configs.size() <= i ? emptyConfigPart() : configs.get(i);
            sourceValues.add(new SourceValue(sourceName, configPart.encoded()));
        }

        return sourceValues;
    }

    private ConfigPart emptyConfigPart() {
        Config config = createConfig();
        return new ConfigPart(config, encode(config));
    }

    protected Config createConfig() {
        return new Config();
    }


    protected abstract void setSource(SourceValue source, boolean createIfNotExists);
    
    protected void setSource(SourceValue source) {
        setSource(source, true);
    }

    protected abstract String getSource(String sourceName);


    protected abstract String encode(Config body);

    protected abstract Config decode(String encoded);


    record SourceValue(String sourceName, String value) {
    }
}
