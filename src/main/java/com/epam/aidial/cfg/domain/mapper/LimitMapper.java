package com.epam.aidial.cfg.domain.mapper;

import java.util.function.Consumer;
import java.util.function.Function;

public abstract class LimitMapper {

    protected <T> void setIfNotNull(Consumer<T> setter, T value) {
        if (value != null) {
            setter.accept(value);
        }
    }

    protected <T, R> T getLimitOrDefault(R limit, R defaultLimit, Function<R, T> limitValueGetter) {
        T limitValue = limitValueGetter.apply(limit);
        return limitValue == null ? limitValueGetter.apply(defaultLimit) : limitValue;
    }
}
