package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.model.source.InterceptorContainerSource;
import com.epam.aidial.cfg.domain.model.source.InterceptorEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.InterceptorRunnerSource;
import com.epam.aidial.cfg.domain.model.source.InterceptorSource;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterceptorValidator {

    private static final String COMPLETION_ENDPOINT_LOG_NAME = "completion";
    private static final String CONFIGURATION_ENDPOINT_LOG_NAME = "configuration";

    private final DeploymentManagerService deploymentManagerService;
    private final DeploymentInfoValidator deploymentInfoValidator;

    @Value("${validation.interceptor.name:}")
    private String interceptorNameValidationPattern;

    public void validateCreation(Interceptor interceptor) {
        validateInterceptorName(interceptor);
        validateInterceptorSource(interceptor);
    }

    public void validateUpdate(String interceptorName, Interceptor interceptor) {
        if (!Objects.equals(interceptorName, interceptor.getName())) {
            throw new IllegalArgumentException("Interceptor with name: '%s' can not be renamed. New interceptor name: '%s'"
                .formatted(interceptorName, interceptor.getName()));
        }
        validateInterceptorSource(interceptor);
    }
    
    private void validateInterceptorName(Interceptor interceptor) {
        final String interceptorName = interceptor.getName();

        if (StringUtils.isEmpty(interceptorNameValidationPattern)) {
            log.debug("Interceptor name validation pattern is empty, skipping validation for interceptor: {}", interceptorName);
            return;
        }

        if (!Pattern.matches(interceptorNameValidationPattern, interceptorName)) {
            throw new IllegalArgumentException("Interceptor name '" + interceptorName
                + "' does not match the required pattern: " + interceptorNameValidationPattern);
        }
    }

    private void validateInterceptorSource(Interceptor interceptor) {
        InterceptorSource source = interceptor.getSource();
        String endpoint = interceptor.getEndpoint();
        String configurationEndpoint = interceptor.getConfigurationEndpoint();

        if (source != null) {
            if (source instanceof InterceptorEndpointsSource) {
                validateEndpointsSource(endpoint, configurationEndpoint);
            } else if (source instanceof InterceptorRunnerSource runnerSource) {
                validateRunnerSource(runnerSource);
            } else if (source instanceof InterceptorContainerSource containerSource) {
                validateContainerSource(containerSource);
            } else {
                throw new IllegalArgumentException(
                    "Unsupported interceptor source: %s. Interceptor: %s".formatted(source, interceptor.getName())
                );
            }
            return;
        }

        validateEndpoint(endpoint, COMPLETION_ENDPOINT_LOG_NAME);
        validateEndpoint(configurationEndpoint, CONFIGURATION_ENDPOINT_LOG_NAME);
    }

    private void validateEndpointsSource(String completionEndpoint, String configurationEndpoint) {
        if (completionEndpoint == null) {
            throw new IllegalArgumentException("Completion endpoint is required when source type is 'Interceptor endpoints'");
        }
        validateEndpoint(completionEndpoint, COMPLETION_ENDPOINT_LOG_NAME);
        validateEndpoint(configurationEndpoint, CONFIGURATION_ENDPOINT_LOG_NAME);
    }

    private void validateRunnerSource(InterceptorRunnerSource runnerSource) {
        if (StringUtils.isBlank(runnerSource.getRunnerName())) {
            throw new IllegalArgumentException("Runner name is required when source type is 'Interceptor runner'");
        }
    }

    private void validateContainerSource(InterceptorContainerSource containerSource) {
        String containerId = containerSource.getContainerId();
        DeploymentInfoDto deploymentInfo = deploymentManagerService.getById(containerId);
        deploymentInfoValidator.validateDeploymentInfo(deploymentInfo, containerId);

        validateEndpointPath(containerSource.getCompletionEndpointPath(), COMPLETION_ENDPOINT_LOG_NAME);
        validateEndpointPath(containerSource.getConfigurationEndpointPath(), CONFIGURATION_ENDPOINT_LOG_NAME);
    }

    private void validateEndpoint(String endpoint, String endpointLogName) {
        if (endpoint != null && EndpointValidator.isInvalidUrl(endpoint)) {
            throw new IllegalArgumentException("Invalid %s endpoint: '%s'".formatted(endpointLogName, endpoint));
        }
    }

    private void validateEndpointPath(String endpoint, String endpointLogName) {
        if (endpoint != null && EndpointValidator.isInvalidUrlPath(endpoint)) {
            throw new IllegalArgumentException("Invalid %s endpoint path: '%s'".formatted(endpointLogName, endpoint));
        }
    }
}