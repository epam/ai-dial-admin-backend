package com.epam.aidial.cfg.utils;

import lombok.experimental.UtilityClass;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

@UtilityClass
public class NullSafeUtils {

    public static <T> void setIfNotNull(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }

    public static <T, R> T getValueOrDefault(R valueHolder, R defaultValueHolder, Function<R, T> valueGetter) {
        T value = valueGetter.apply(valueHolder);
        return value == null ? valueGetter.apply(defaultValueHolder) : value;
    }

    public static <T> T createIfNull(T value, Supplier<T> factory) {
        if (value != null) {
            return value;
        }
        return factory.get();
    }
}
