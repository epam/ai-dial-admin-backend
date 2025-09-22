package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.client.dto.DeploymentInfoDto;
import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.model.source.InterceptorContainerSource;
import com.epam.aidial.cfg.domain.model.source.InterceptorEndpointsSource;
import com.epam.aidial.cfg.domain.model.source.InterceptorRunnerSource;
import com.epam.aidial.cfg.domain.model.source.InterceptorSource;
import com.epam.aidial.cfg.domain.service.DeploymentManagerService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
@Component
public class InterceptorValidator {

    private static final String COMPLETION_ENDPOINT_LOG_NAME = "completion";
    private static final String CONFIGURATION_ENDPOINT_LOG_NAME = "configuration";

    private final DeploymentManagerService deploymentManagerService;
    private final DeploymentInfoValidator deploymentInfoValidator;
    private final IdFieldValidator idFieldValidator;

    private final String interceptorNameValidationPattern;

    public InterceptorValidator(DeploymentManagerService deploymentManagerService,
                                DeploymentInfoValidator deploymentInfoValidator,
                                IdFieldValidator idFieldValidator,
                                @Value("${validation.interceptor.name:}") String interceptorNameValidationPattern) {
        this.deploymentManagerService = deploymentManagerService;
        this.deploymentInfoValidator = deploymentInfoValidator;
        this.idFieldValidator = idFieldValidator;
        this.interceptorNameValidationPattern = interceptorNameValidationPattern;
    }

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

        idFieldValidator.validateName("Interceptor", interceptorName);

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
        String interceptorName = interceptor.getName();
        String endpoint = interceptor.getEndpoint();
        String configurationEndpoint = interceptor.getFeatures() != null
                ? interceptor.getFeatures().getConfigurationEndpoint()
                : null;

        if (source != null) {
            if (source instanceof InterceptorEndpointsSource) {
                validateEndpointsSource(endpoint, configurationEndpoint, interceptorName);
            } else if (source instanceof InterceptorRunnerSource runnerSource) {
                validateRunnerSource(runnerSource, interceptorName);
            } else if (source instanceof InterceptorContainerSource containerSource) {
                validateContainerSource(containerSource, interceptorName);
            } else {
                throw new IllegalArgumentException(
                        "Unsupported interceptor source: %s. Interceptor: %s".formatted(source, interceptorName)
                );
            }
            return;
        }

        validateEndpoint(endpoint, COMPLETION_ENDPOINT_LOG_NAME, interceptorName);
        validateEndpoint(configurationEndpoint, CONFIGURATION_ENDPOINT_LOG_NAME, interceptorName);
    }

    private void validateEndpointsSource(String completionEndpoint, String configurationEndpoint, String interceptorName) {
        if (completionEndpoint == null) {
            throw new IllegalArgumentException("Completion endpoint is required when source type is 'Interceptor endpoints'. Interceptor: %s"
                    .formatted(interceptorName));
        }
        validateEndpoint(completionEndpoint, COMPLETION_ENDPOINT_LOG_NAME, interceptorName);
        validateEndpoint(configurationEndpoint, CONFIGURATION_ENDPOINT_LOG_NAME, interceptorName);
    }

    private void validateRunnerSource(InterceptorRunnerSource runnerSource, String interceptorName) {
        if (StringUtils.isBlank(runnerSource.getRunnerName())) {
            throw new IllegalArgumentException("Runner name is required when source type is 'Interceptor runner'. Interceptor: %s"
                    .formatted(interceptorName));
        }
    }

    private void validateContainerSource(InterceptorContainerSource containerSource, String interceptorName) {
        String containerId = containerSource.getContainerId();
        DeploymentInfoDto deploymentInfo = deploymentManagerService.getById(containerId);
        deploymentInfoValidator.validateDeploymentInfo(deploymentInfo, containerId);

        validateEndpointPath(containerSource.getCompletionEndpointPath(), COMPLETION_ENDPOINT_LOG_NAME, interceptorName);
        validateEndpointPath(containerSource.getConfigurationEndpointPath(), CONFIGURATION_ENDPOINT_LOG_NAME, interceptorName);
    }

    private void validateEndpoint(String endpoint, String endpointLogName, String interceptorName) {
        if (endpoint != null && EndpointValidator.isInvalidUrl(endpoint)) {
            throw new IllegalArgumentException("Invalid %s endpoint: '%s'. Interceptor: %s"
                    .formatted(endpointLogName, endpoint, interceptorName));
        }
    }

    private void validateEndpointPath(String endpoint, String endpointLogName, String interceptorName) {
        if (endpoint != null && EndpointValidator.isInvalidUrlPath(endpoint)) {
            throw new IllegalArgumentException("Invalid %s endpoint path: '%s'. Interceptor: %s"
                    .formatted(endpointLogName, endpoint, interceptorName));
        }
    }
}