package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Interceptor;
import com.epam.aidial.cfg.domain.model.Source;
import com.epam.aidial.cfg.domain.model.SourceType;
import com.epam.aidial.cfg.domain.service.ExternalDeploymentScheduledService;
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

    private final ExternalDeploymentScheduledService deploymentService;

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

    // TODO [VPA]: refactor
    private void validateInterceptorSource(Interceptor interceptor) {
        Source source = interceptor.getSource();
        String endpoint = interceptor.getEndpoint();
        String configurationEndpoint = interceptor.getConfigurationEndpoint();

        if (configurationEndpoint != null && EndpointValidator.isInvalidUrl(configurationEndpoint)) {
            throw new IllegalArgumentException("Invalid configuration endpoint: '%s'".formatted(configurationEndpoint));
        }

        if (source == null) {
            // TODO [VPA]: uncomment validations when FE will support interceptor runners
            //if (endpoint == null) {
            //    throw new IllegalArgumentException("Missing endpoint and source. At least one of them should be specified");
            //}
            //if (StringUtils.isBlank(endpoint)) {
            //    throw new IllegalArgumentException("Invalid endpoint: '%s'".formatted(endpoint));
            //}
            return;
        }

        SourceType sourceType = source.getType();
        String sourceName = source.getName();

        if (sourceName == null) {
            throw new IllegalArgumentException("Source name is required when source is specified");
        }

        switch (sourceType) {
            case ENDPOINTS:
                if (endpoint == null) {
                    throw new IllegalArgumentException(
                        "Endpoint is required when source type is %s".formatted(sourceType.getDescription())
                    );
                }
                break;
            case TEMPLATE:
                if (endpoint != null) {
                    throw new IllegalArgumentException(
                        "Both endpoint: '%s' and interceptor runner: '%s' are specified. Only one of them should be specified"
                            .formatted(endpoint, sourceName)
                    );
                }
                break;
            case CONTAINER:
                var deploymentInfo = deploymentService.getById(sourceName);
                if (deploymentInfo == null) {
                    throw new IllegalArgumentException("Container with name '%s' not found".formatted(sourceName));
                }

                var deploymentUrl = deploymentInfo.getUrl();
                if (deploymentUrl != null) {
                    if (endpoint != null  && !endpoint.startsWith(deploymentUrl)) {
                        throw new IllegalArgumentException("Completion endpoint should start with '%s' but was: %s"
                                .formatted(deploymentUrl, endpoint));
                    }
                    if (configurationEndpoint != null && !configurationEndpoint.startsWith(deploymentUrl)) {
                        throw new IllegalArgumentException("Configuration endpoint should start with '%s' but was: %s"
                                .formatted(deploymentUrl, configurationEndpoint));
                    }
                }
                break;
            default:
                throw new IllegalArgumentException("Unsupported source type: " + sourceType);
        }
    }
}