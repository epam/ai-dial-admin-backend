package com.epam.aidial.metric.util;

import lombok.experimental.UtilityClass;

import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@UtilityClass
public class CollectorsUtils {

    public static <T> Collector<T, ?, Optional<T>> toSingleton(Supplier<? extends RuntimeException> exceptionSupplier) {
        return Collectors.collectingAndThen(
                Collectors.toList(),
                list -> {
                    if (list.size() > 1) {
                        throw exceptionSupplier.get();
                    }
                    if (list.isEmpty()) {
                        return Optional.empty();
                    }

                    return Optional.of(list.get(0));
                }
        );
    }

}
