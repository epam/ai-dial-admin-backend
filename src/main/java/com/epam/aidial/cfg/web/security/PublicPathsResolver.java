package com.epam.aidial.cfg.web.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Component
@ConditionalOnExpression(("'${config.rest.security.mode}' == 'oidc' OR '${config.rest.security.mode}' == 'basic'"))
public class PublicPathsResolver {

    private final boolean disableSwaggerAuthorization;

    public PublicPathsResolver(@Value("${config.rest.security.disable-swagger-authorization}") boolean disableSwaggerAuthorization) {
        this.disableSwaggerAuthorization = disableSwaggerAuthorization;
    }

    protected String[] resolvePublicPathPatterns() {
        var swaggerPaths = disableSwaggerAuthorization
                ? List.of("/swagger-ui/**", "/v3/api-docs/**")
                : List.<String>of();
        var appHealthPaths = List.of("/api/v1/health/**");

        return Stream.of(swaggerPaths, appHealthPaths)
                .flatMap(Collection::stream)
                .toArray(String[]::new);
    }
}
