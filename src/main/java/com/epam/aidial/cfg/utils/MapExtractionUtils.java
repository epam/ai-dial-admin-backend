package com.epam.aidial.cfg.utils;

import lombok.experimental.UtilityClass;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@UtilityClass
public final class MapExtractionUtils {

    public static <K, V> Optional<V> extractFirstNonNullValue(
            Map<K, V> source,
            List<K> keys) {

        if (MapUtils.isEmpty(source) || CollectionUtils.isEmpty(keys)) {
            return Optional.empty();
        }

        return keys.stream()
                .map(source::get)
                .filter(Objects::nonNull)
                .findFirst();
    }
}