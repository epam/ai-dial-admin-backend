package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.InterceptorRunner;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class InterceptorRunnerValidator {

    public void validateCreation(InterceptorRunner interceptorRunner) {
        validateEndpoints(interceptorRunner.getCompletionEndpoint(), interceptorRunner.getConfigurationEndpoint());
    }

    public void validateUpdate(String interceptorRunnerName, InterceptorRunner interceptorRunner) {
        if (!Objects.equals(interceptorRunnerName, interceptorRunner.getName())) {
            throw new IllegalArgumentException("InterceptorRunner with name: '%s' can not be renamed. New interceptor runner name: '%s'"
                .formatted(interceptorRunnerName, interceptorRunner.getName()));
        }
        validateEndpoints(interceptorRunner.getCompletionEndpoint(), interceptorRunner.getConfigurationEndpoint());
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