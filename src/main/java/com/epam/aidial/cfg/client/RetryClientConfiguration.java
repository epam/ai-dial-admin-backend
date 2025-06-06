package com.epam.aidial.cfg.client;

import feign.FeignException;
import feign.RetryableException;
import feign.Retryer;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

public class RetryClientConfiguration {

    @Value("${feign.retry.period}")
    private int period;
    @Value("${feign.retry.maxPeriod}")
    private int maxPeriod;
    @Value("${feign.retry.maxAttempts}")
    private int maxAttempts;
    @Value("${feign.retry.errorCodes}")
    private String errorCodes;

    @Bean
    public Retryer retryer() {
        return new LoggingRetryer(period, maxPeriod, maxAttempts);
    }

    @Bean
    public ErrorDecoder errorDecoder() {
        var defaultErrorDecoder = new ErrorDecoder.Default();
        var retryableCodes = getRetryableErrorCodes();

        return (methodKey, response) -> {
            if (retryableCodes.contains(response.status())) {
                var cause = FeignException.errorStatus(methodKey, response);

                return new RetryableException(
                        response.status(),
                        cause.getMessage(),
                        response.request().httpMethod(),
                        cause,
                        (Long) null,
                        response.request()
                );
            }

            return defaultErrorDecoder.decode(methodKey, response);
        };
    }

    private Set<Integer> getRetryableErrorCodes() {
        return Arrays.stream(errorCodes.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toSet());
    }

    @Slf4j
    private static class LoggingRetryer implements Retryer {

        private final long period;
        private final long maxPeriod;
        private final int maxAttempts;
        private final Retryer retryer;

        private LoggingRetryer(long period, long maxPeriod, int maxAttempts) {
            this.period = period;
            this.maxPeriod = maxPeriod;
            this.maxAttempts = maxAttempts;
            this.retryer = new Retryer.Default(period, maxPeriod, maxAttempts);
        }

        @Override
        public void continueOrPropagate(RetryableException e) {
            log.warn("Error occurred. Retrying...", e);
            retryer.continueOrPropagate(e);
        }

        @Override
        public Retryer clone() {
            return new LoggingRetryer(period, maxPeriod, maxAttempts);
        }
    }

}
