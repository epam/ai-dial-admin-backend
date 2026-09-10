package com.epam.aidial.cfg.service.config.impl.storage;

import com.epam.aidial.core.config.Assistants;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreAssistant;
import com.epam.aidial.core.config.CoreRoute;
import com.google.common.collect.Lists;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Component
public class ConfigSplitter {

    public List<ConfigPart> splitConfig(Config configBody, Function<Config, String> encoder, int maxSize, int partitioningLimit) {
        return splitConfig(encoder, maxSize, Stream.of(
                                mapEntries(configBody, this::getRoutes, this::getSetRoutes, LinkedHashMap::new),
                                mapEntries(configBody, Config::getModels, Config::setModels),
                                mapEntries(configBody, Config::getAddons, Config::setAddons),
                                mapEntries(configBody, Config::getApplications, Config::setApplications),
                                mapAssistantEndpoint(configBody),
                                mapAssistantFeatures(configBody),
                                mapAssistants(configBody),
                                mapEntries(configBody, Config::getKeys, Config::setKeys),
                                mapEntries(configBody, Config::getRoles, Config::setRoles),
                                mapRetriableErrorCodes(configBody),
                                mapEntries(configBody, Config::getInterceptors, Config::setInterceptors),
                                mapEntries(configBody, Config::getApplicationTypeSchemas, Config::setApplicationTypeSchemas),
                                mapEntries(configBody, Config::getToolsets, Config::setToolsets))
                        .flatMap(Function.identity())
                        .toList(),
                partitioningLimit);
    }

    private List<ConfigPart> splitConfig(Function<Config, String> encoder, int maxSize, List<ConfigEntry> entries, int partitioningLimit) {

        for (int i = 1; i <= partitioningLimit; i++) {
            int partSize = (int) Math.ceil(entries.size() / (double) i);
            List<ConfigPart> configs = trySplit(encoder, maxSize, entries, partSize);
            if (configs == null) {
                continue;
            }
            return configs;
        }
        throw new IllegalStateException("Unable to split config to " + partitioningLimit + " part(s) with maxSize " + maxSize + ". "
                + buildErrorDetail(encoder, maxSize, entries, partitioningLimit));
    }

    private List<ConfigPart> trySplit(Function<Config, String> encoder, int maxSize, List<ConfigEntry> entries, int partSize) {
        List<List<ConfigEntry>> parts = Lists.partition(entries, partSize);
        List<ConfigPart> configs = new ArrayList<>();
        for (List<ConfigEntry> configEntries : parts) {
            Config config = createConfig();
            for (ConfigEntry entry : configEntries) {
                entry.put().accept(config);
            }
            ConfigUtils.removeEmptyCollections(config);
            String encoded = encoder.apply(config);
            ConfigPart configPart = new ConfigPart(config, encoded);
            if (encoded.getBytes().length > maxSize) {
                return null;
            }
            configs.add(configPart);
        }
        return configs;
    }

    private String buildErrorDetail(Function<Config, String> encoder, int maxSize,
                                    List<ConfigEntry> entries, int partitioningLimit) {
        long totalSize = 0;
        List<EntrySizeInfo> entrySizes = new ArrayList<>();
        for (int i = 0; i < entries.size(); i++) {
            Config config = createConfig();
            entries.get(i).put().accept(config);
            ConfigUtils.removeEmptyCollections(config);
            int size = encoder.apply(config).getBytes().length;
            totalSize += size;
            String label = entries.get(i).label() != null ? entries.get(i).label() : "entry[" + i + "]";
            entrySizes.add(new EntrySizeInfo(label, size));
        }
        long capacity = (long) partitioningLimit * maxSize;
        List<String> topEntries = entrySizes.stream()
                .sorted(Comparator.comparingInt(EntrySizeInfo::size).reversed())
                .limit(5)
                .map(e -> e.label() + "=" + e.size() + "B")
                .toList();
        return ("Total encoded size %d bytes exceeds available capacity %d bytes "
                + "(%d destinations x %d maxSize). Largest entries: %s")
                .formatted(totalSize, capacity, partitioningLimit, maxSize, topEntries);
    }

    private Stream<ConfigEntry> mapRetriableErrorCodes(Config configBody) {
        Consumer<Config> put = config -> config.setRetriableErrorCodes(configBody.getRetriableErrorCodes());
        return Stream.of(new ConfigEntry(put, "retriableErrorCodes"));
    }

    private Stream<ConfigEntry> mapAssistantEndpoint(Config configBody) {
        if (configBody.getAssistant() != null && configBody.getAssistant().getEndpoint() != null) {
            Consumer<Config> putEndpoint = config -> {
                if (config.getAssistant() == null) {
                    config.setAssistant(new Assistants());
                }
                config.getAssistant().setEndpoint(configBody.getAssistant().getEndpoint());
            };
            return Stream.of(new ConfigEntry(putEndpoint, "assistant.endpoint"));
        }
        return Stream.empty();
    }

    private Stream<ConfigEntry> mapAssistantFeatures(Config configBody) {
        if (configBody.getAssistant() != null && configBody.getAssistant().getFeatures() != null) {
            Consumer<Config> putFeatures = config -> {
                if (config.getAssistant() == null) {
                    config.setAssistant(new Assistants());
                }
                config.getAssistant().setFeatures(configBody.getAssistant().getFeatures());
            };
            return Stream.of(new ConfigEntry(putFeatures, "assistant.features"));
        }
        return Stream.empty();
    }

    private Stream<ConfigEntry> mapAssistants(Config configBody) {
        Function<Config, Map<String, CoreAssistant>> getter = config -> {
            if (config.getAssistant() == null) {
                config.setAssistant(new Assistants());
            }
            return config.getAssistant().getAssistants();
        };
        BiConsumer<Config, Map<String, CoreAssistant>> setter = (config, assistants) -> {
            if (config.getAssistant() == null) {
                config.setAssistant(new Assistants());
            }
            config.getAssistant().setAssistants(assistants);
        };
        return mapEntries(configBody, getter, setter);
    }

    private <T> Stream<ConfigEntry> mapEntries(Config configBody,
                                               Function<Config, Map<String, T>> getter,
                                               BiConsumer<Config, Map<String, T>> setter) {
        return mapEntries(configBody, getter, setter, HashMap::new);
    }

    private <T> Stream<ConfigEntry> mapEntries(Config configBody,
                                               Function<Config, Map<String, T>> getter,
                                               BiConsumer<Config, Map<String, T>> setter,
                                               Supplier<Map<String, T>> mapFactory) {
        return getter.apply(configBody)
                .entrySet()
                .stream()
                .map(e -> new ConfigEntry(
                        config -> {
                            Map<String, T> map = getter.apply(config);
                            if (map == null) {
                                map = mapFactory.get();
                                setter.accept(config, map);
                            }
                            map.put(e.getKey(), e.getValue());
                        },
                        e.getKey())
                );
    }

    record ConfigEntry(Consumer<Config> put, String label) {
    }

    private static Config createConfig() {
        Config config = new Config();
        config.setRoutes(null);
        config.setModels(null);
        config.setAddons(null);
        config.setApplications(null);
        config.setToolsets(null);
        config.setAssistant(null);
        config.setKeys(null);
        config.setRoles(null);
        config.setInterceptors(null);
        config.setApplicationTypeSchemas(null);
        config.setRetriableErrorCodes(null);
        return config;
    }

    private void getSetRoutes(Config config, Map<String, CoreRoute> routes) {
        if (routes instanceof LinkedHashMap<String, CoreRoute> map) {
            config.setRoutes(map);
        } else {
            throw new IllegalArgumentException("routes is not linked hashmap " + routes);
        }
    }

    private Map<String, CoreRoute> getRoutes(Config config) {
        return config.getRoutes();
    }

    private record EntrySizeInfo(String label, int size) {}
}
