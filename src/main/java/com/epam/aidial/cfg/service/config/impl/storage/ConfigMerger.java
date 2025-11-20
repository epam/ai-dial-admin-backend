package com.epam.aidial.cfg.service.config.impl.storage;

import com.epam.aidial.core.config.Assistants;
import com.epam.aidial.core.config.Config;
import com.epam.aidial.core.config.CoreAssistant;
import com.epam.aidial.core.config.CoreRoute;
import lombok.NonNull;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

/**
 * Re-assembles several Config fragments that were previously produced by
 * {@link ConfigSplitter} into one logical configuration.
 */
@Component
public class ConfigMerger {

    public Config merge(@NonNull List<String> encodedParts, Function<String, Config> decoder) {

        if (encodedParts.isEmpty()) {
            return new Config();
        }

        Config config = new Config();

        for (String encodedPart : encodedParts) {
            var part = decoder.apply(encodedPart);
            mergeRoutes(part, config);
            mergeModels(part, config);
            mergeAddons(part, config);
            mergeApplications(part, config);
            mergeToolSets(part, config);
            mergeAssistants(part, config);
            mergeKeys(part, config);
            mergeRoles(part, config);
            mergeInterceptors(part, config);
            mergeApplicationTypeSchemas(part, config);
            mergeRetriableErrorCodes(part, config);
        }

        ConfigUtils.removeEmptyCollections(config);

        return config;
    }

    private void mergeRoutes(Config from, Config to) {
        if (MapUtils.isNotEmpty(from.getRoutes())) {
            LinkedHashMap<String, CoreRoute> target =
                    to.getRoutes() == null ? new LinkedHashMap<>() : to.getRoutes();
            target.putAll(from.getRoutes());
            to.setRoutes(target);
        }
    }

    private void mergeModels(Config from, Config to) {
        mergeGenericMap(from.getModels(), to::getModels, to::setModels);
    }

    private void mergeAddons(Config from, Config to) {
        mergeGenericMap(from.getAddons(), to::getAddons, to::setAddons);
    }

    private void mergeApplications(Config from, Config to) {
        mergeGenericMap(from.getApplications(), to::getApplications, to::setApplications);
    }

    private void mergeKeys(Config from, Config to) {
        mergeGenericMap(from.getKeys(), to::getKeys, to::setKeys);
    }

    private void mergeRoles(Config from, Config to) {
        mergeGenericMap(from.getRoles(), to::getRoles, to::setRoles);
    }

    private void mergeInterceptors(Config from, Config to) {
        mergeGenericMap(from.getInterceptors(), to::getInterceptors, to::setInterceptors);
    }

    private void mergeApplicationTypeSchemas(Config from, Config to) {
        mergeGenericMap(from.getApplicationTypeSchemas(),
                to::getApplicationTypeSchemas,
                to::setApplicationTypeSchemas);
    }

    private void mergeToolSets(Config from, Config to) {
        mergeGenericMap(from.getToolsets(), to::getToolsets, to::setToolsets);
    }

    private <T> void mergeGenericMap(Map<String, T> src,
                                     java.util.function.Supplier<Map<String, T>> getter,
                                     java.util.function.Consumer<Map<String, T>> setter) {

        if (MapUtils.isEmpty(src)) {
            return;
        }

        Map<String, T> target = getter.get();
        if (target == null) {
            target = new HashMap<>();
            setter.accept(target);
        }
        target.putAll(src);
    }

    private void mergeAssistants(Config from, Config to) {

        Assistants src = from.getAssistant();
        if (src == null) {
            return;
        }

        Assistants target = to.getAssistant();
        if (target == null) {
            target = new Assistants();
            to.setAssistant(target);
        }

        if (src.getEndpoint() != null) {
            target.setEndpoint(src.getEndpoint());
        }
        if (src.getFeatures() != null) {
            target.setFeatures(src.getFeatures());
        }
        if (MapUtils.isNotEmpty(src.getAssistants())) {
            Map<String, CoreAssistant> map = target.getAssistants();
            if (map == null) {
                map = new HashMap<>();
                target.setAssistants(map);
            }
            map.putAll(src.getAssistants());
        }
    }

    private void mergeRetriableErrorCodes(Config from, Config to) {
        if (CollectionUtils.isEmpty(from.getRetriableErrorCodes())) {
            return;
        }
        Set<Integer> target = to.getRetriableErrorCodes();
        if (target == null) {
            target = new HashSet<>();
            to.setRetriableErrorCodes(target);
        }
        target.addAll(from.getRetriableErrorCodes());
    }

}