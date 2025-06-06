package com.epam.aidial.cfg.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.function.Function;
import java.util.function.Supplier;

@Slf4j
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class SimpleCircuitBreaker {

    private final int errorsThreshold;

    private int consecutiveErrors = 0;

    SimpleCircuitBreaker(@Value("${prompts.import.consecutiveErrorsThreshold}") int errorsThreshold) {
        this.errorsThreshold = errorsThreshold;
    }

    public <T> T apply(Supplier<T> supplier, Function<Exception, T> fallback) {
        if (consecutiveErrors >= errorsThreshold) {
            log.warn("Circuit breaker is OPEN (threshold: {}). Skipping execution and calling fallback.", errorsThreshold);
            return fallback.apply(null);
        }

        try {
            var result = supplier.get();
            consecutiveErrors = 0;
            return result;
        } catch (Exception ex) {
            consecutiveErrors++;
            log.error("Error occurred (attempt {}/{}). Executing fallback.", consecutiveErrors, errorsThreshold, ex);
            return fallback.apply(ex);
        }
    }

}
