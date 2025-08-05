package com.epam.aidial.cfg.security.s2s;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.SimpleTokenCache;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
@Slf4j
public class InnerSystemUserSecurityConfig {

    @Bean
    public SimpleTokenCache thisServiceToDeploymentManagerScopeToken(
            @Value("${plugins.deployment.manager.scope}") final String deploymentManagerScope,
            final TokenCredential tokenCredential) {
        final var tokenRequest = new TokenRequestContext().addScopes(deploymentManagerScope);
        return new SimpleTokenCache(() -> getTokenMono(tokenCredential, tokenRequest));
    }

    private Mono<AccessToken> getTokenMono(final TokenCredential tokenCredential,
                                           final TokenRequestContext tokenRequest) {
        return tokenCredential
            .getToken(tokenRequest)
            .doOnError(e -> log.error("Error acquiring token from AAD for scope {}", tokenRequest.getScopes(), e));
    }
}
