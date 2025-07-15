package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Interceptor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.regex.Pattern;

@Slf4j
@Component
public class InterceptorValidator {

    @Value("${validation.interceptor.name:}")
    private String interceptorNameValidationPattern;

    public void validateCreation(Interceptor interceptor) {
        validateInterceptorName(interceptor);
        validateInterceptorRunnerAndEndpoint(interceptor);
    }

    public void validateUpdate(String interceptorName, Interceptor interceptor) {
        if (!Objects.equals(interceptorName, interceptor.getName())) {
            throw new IllegalArgumentException("Interceptor with name: '%s' can not be renamed. New interceptor name: '%s'"
                .formatted(interceptorName, interceptor.getName()));
        }
        validateInterceptorRunnerAndEndpoint(interceptor);
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

    private void validateInterceptorRunnerAndEndpoint(Interceptor interceptor) {
        String endpoint = interceptor.getEndpoint();
        String configurationEndpoint = interceptor.getConfigurationEndpoint();
        String interceptorRunner = interceptor.getInterceptorRunner();

        // TODO [VPA]: uncomment validations when FE will support interceptor runners
        //if (endpoint != null && StringUtils.isBlank(endpoint)) {
        //    throw new IllegalArgumentException("Invalid endpoint: '%s'".formatted(endpoint));
        //}

        if (configurationEndpoint != null && EndpointValidator.isInvalidUrl(configurationEndpoint)) {
            throw new IllegalArgumentException("Invalid configuration endpoint: '%s'".formatted(configurationEndpoint));
        }

        //if (endpoint == null && StringUtils.isBlank(interceptorRunner)) {
        //    throw new IllegalArgumentException("Missing endpoint and interceptor runner. At least one of them should be specified");
        //}

        if (endpoint != null && StringUtils.isNotBlank(interceptorRunner)) {
            throw new IllegalArgumentException(
                "Both endpoint: '%s' and interceptor runner: '%s' are specified. Only one of them should be specified"
                    .formatted(endpoint, interceptorRunner)
            );
        }
    }
}
