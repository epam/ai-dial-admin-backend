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

    public void validateCreation(InterceptorRunner interceptorRunner) {
        validateInterceptorRunnerName(interceptorRunner);
        validateEndpoints(interceptorRunner.getCompletionEndpoint(), interceptorRunner.getConfigurationEndpoint());
    }

    public void validateUpdate(String interceptorRunnerName, InterceptorRunner interceptorRunner) {
        if (!Objects.equals(interceptorRunnerName, interceptorRunner.getName())) {
            throw new IllegalArgumentException("InterceptorRunner with name: '%s' can not be renamed. New interceptor runner name: '%s'"
                    .formatted(interceptorRunnerName, interceptorRunner.getName()));
        }
        validateEndpoints(interceptorRunner.getCompletionEndpoint(), interceptorRunner.getConfigurationEndpoint());
    }

    private void validateInterceptorRunnerName(InterceptorRunner interceptorRunner) {
        final String interceptorRunnerName = interceptorRunner.getName();

        idFieldValidator.validateName("InterceptorRunner", interceptorRunnerName);

        if (StringUtils.isEmpty(interceptorRunnerNameValidationPattern)) {
            log.debug("InterceptorRunner name validation pattern is empty, skipping validation for interceptor runner: {}", interceptorRunnerName);
            return;
        }

        if (!Pattern.matches(interceptorRunnerNameValidationPattern, interceptorRunnerName)) {
            throw new IllegalArgumentException("InterceptorRunner name '" + interceptorRunnerName
                    + "' does not match the required pattern: " + interceptorRunnerNameValidationPattern);
        }
    }

    private void validateEndpoints(String completionEndpoint, String configurationEndpoint) {
        if (completionEndpoint != null && EndpointValidator.isInvalidUrl(completionEndpoint)) {
            throw new IllegalArgumentException("Invalid completion endpoint: '%s'".formatted(completionEndpoint));
        }
        if (configurationEndpoint != null && EndpointValidator.isInvalidUrl(configurationEndpoint)) {
            throw new IllegalArgumentException("Invalid configuration endpoint: '%s'".formatted(configurationEndpoint));
        }
    }
}