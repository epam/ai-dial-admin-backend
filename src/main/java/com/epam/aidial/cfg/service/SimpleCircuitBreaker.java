package com.epam.aidial.cfg.service;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
public class SimpleCircuitBreaker {

    private final int errorsThreshold;

    private int consecutiveErrors = 0;

    public SimpleCircuitBreaker(int errorsThreshold) {
        this.errorsThreshold = errorsThreshold;
    }

    public <T> T apply(Supplier<T> supplier, Function<Exception, T> exceptionFallback, Supplier<T> thresholdExceededFallback) {
        if (consecutiveErrors >= errorsThreshold) {
            log.warn("Circuit breaker is OPEN (threshold: {}). Skipping execution and calling fallback.", errorsThreshold);
            return thresholdExceededFallback.get();
        }

        try {
            var result = supplier.get();
            consecutiveErrors = 0;
            return result;
        } catch (Exception ex) {
            consecutiveErrors++;
            log.error("Error occurred (attempt {}/{}). Executing fallback.", consecutiveErrors, errorsThreshold, ex);
            return exceptionFallback.apply(ex);
        }
    }

}
