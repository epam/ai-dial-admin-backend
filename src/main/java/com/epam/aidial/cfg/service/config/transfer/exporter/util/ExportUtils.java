package com.epam.aidial.cfg.service.config.transfer.exporter.util;

import com.epam.aidial.cfg.domain.model.ExportConfigComponentType;
import com.epam.aidial.cfg.model.ExportConfigComponent;
import lombok.experimental.UtilityClass;
import org.apache.commons.collections4.CollectionUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@UtilityClass
public class ExportUtils {

    public static boolean hasAnyRequestedTopic(Set<String> entityTopics, Set<String> requestedTopics) {
        return CollectionUtils.isEmpty(requestedTopics)
                || (entityTopics != null && requestedTopics.stream().anyMatch(entityTopics::contains));
    }

    public static <T> Collector<T, ?, LinkedHashMap<String, T>> toLinkedHashMap(Function<T, String> keyExtractor) {
        return Collectors.toMap(
                keyExtractor,
                Function.identity(),
                (a, b) -> {
                    throw new IllegalStateException("Duplicated keys found: %s".formatted(a));
                },
                LinkedHashMap::new
        );
    }

    public static Map<String, ExportConfigComponent> filterComponentsByTypeAndCollectToMap(
            List<ExportConfigComponent> components,
            ExportConfigComponentType type) {
        return components.stream()
                .filter(component -> component.getType() == type)
                .collect(Collectors.toMap(
                        ExportConfigComponent::getName,
                        Function.identity(),
                        (existing, replacement) -> {
                            existing.addDependencies(replacement.getDependencies());
                            return existing;
                        }
                ));
    }
}
