package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.InterceptorRunner;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
@Component
public class InterceptorRunnerValidator {

    private final IdFieldValidator idFieldValidator;

    private final String interceptorRunnerNameValidationPattern;

    public InterceptorRunnerValidator(IdFieldValidator idFieldValidator,
                                      @Value("${validation.interceptorRunner.name:}") String interceptorRunnerNameValidationPattern) {
        this.idFieldValidator = idFieldValidator;
        this.interceptorRunnerNameValidationPattern = interceptorRunnerNameValidationPattern;
    }

    public void validateCreation(InterceptorRunner runner) {
        validateInterceptorRunnerName(runner);
        validateEndpoints(runner.getCompletionEndpoint(), runner.getConfigurationEndpoint(), runner.getName());
    }

    public void validateUpdate(String interceptorRunnerName, InterceptorRunner runner) {
        if (!Objects.equals(interceptorRunnerName, runner.getName())) {
            throw new IllegalArgumentException("Interceptor runner with name: '%s' can not be renamed. New interceptor runner name: '%s'"
                    .formatted(interceptorRunnerName, runner.getName()));
        }
        validateEndpoints(runner.getCompletionEndpoint(), runner.getConfigurationEndpoint(), runner.getName());
    }

    private void validateInterceptorRunnerName(InterceptorRunner interceptorRunner) {
        final String interceptorRunnerName = interceptorRunner.getName();

        idFieldValidator.validateName("InterceptorRunner", interceptorRunnerName);

        if (StringUtils.isEmpty(interceptorRunnerNameValidationPattern)) {
            log.debug("Interceptor runner name validation pattern is empty, skipping validation for interceptor runner: {}", interceptorRunnerName);
            return;
        }

        if (!Pattern.matches(interceptorRunnerNameValidationPattern, interceptorRunnerName)) {
            throw new IllegalArgumentException("Interceptor runner name '" + interceptorRunnerName
                    + "' does not match the required pattern: " + interceptorRunnerNameValidationPattern);
        }
    }

    private void validateEndpoints(String completionEndpoint, String configurationEndpoint, String runnerName) {
        if (completionEndpoint != null && EndpointValidator.isInvalidUrl(completionEndpoint)) {
            throw new IllegalArgumentException("Invalid completion endpoint: '%s'. Interceptor runner: %s"
                    .formatted(completionEndpoint, runnerName));
        }
        if (configurationEndpoint != null && EndpointValidator.isInvalidUrl(configurationEndpoint)) {
            throw new IllegalArgumentException("Invalid configuration endpoint: '%s'. Interceptor runner: %s"
                    .formatted(configurationEndpoint, runnerName));
        }
    }
}