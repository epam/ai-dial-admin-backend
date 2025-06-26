package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.Interceptor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.util.Objects;

@Component
public class InterceptorValidator {

    public void validateCreation(Interceptor interceptor) {
        validateInterceptorRunnerAndEndpoint(interceptor);
    }

    public void validateUpdate(String interceptorName, Interceptor interceptor) {
        if (!Objects.equals(interceptorName, interceptor.getName())) {
            throw new IllegalArgumentException("Interceptor with name: '%s' can not be renamed. New interceptor name: '%s'"
                .formatted(interceptorName, interceptor.getName()));
        }
        validateInterceptorRunnerAndEndpoint(interceptor);
    }

    private void validateInterceptorRunnerAndEndpoint(Interceptor interceptor) {
        String endpoint = interceptor.getEndpoint();
        String configurationEndpoint = interceptor.getConfigurationEndpoint();
        String interceptorRunner = interceptor.getInterceptorRunner();

        if (configurationEndpoint != null && EndpointValidator.isInvalidUrl(configurationEndpoint)) {
            throw new IllegalArgumentException("Invalid configuration endpoint: '%s'".formatted(configurationEndpoint));
        }

        if (endpoint != null && StringUtils.isNotBlank(interceptorRunner)) {
            throw new IllegalArgumentException(
                "Both endpoint: '%s' and interceptor runner: '%s' are specified. Only one of them should be specified"
                    .formatted(endpoint, interceptorRunner)
            );
        }
    }
}
