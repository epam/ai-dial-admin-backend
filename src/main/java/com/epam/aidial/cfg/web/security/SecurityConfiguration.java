package com.epam.aidial.cfg.web.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@ConditionalOnProperty(value = "config.rest.security.mode", havingValue = "oidc", matchIfMissing = true)
@RequiredArgsConstructor
@Slf4j
public class SecurityConfiguration {

    private final JwtProvidersProperties jwtProviderProperties;
    private final JwtProviderUtils jwtProviderUtils;

    @Value("${config.rest.security.default.allowedRoles}")
    protected Set<String> defaultAllowedRoles;

    @Value("${config.rest.security.disable-swagger-authorization}")
    protected boolean disableSwaggerAuthorization;

    @Bean
    public Map<String, Set<String>> allowedRolesByIssuer() {
        Map<String, Set<String>> tmpRolesByIssuer = new HashMap<>();
        var providers = jwtProviderProperties.getProviders();
        providers.forEach((name, config) -> {
            Set<String> acceptedRoles = new HashSet<>(defaultAllowedRoles);
            if (config.getAllowedRoles() != null) {
                acceptedRoles.addAll(config.getAllowedRoles());
            }
            var acceptedIssuers = jwtProviderUtils.getAcceptedIssuers(config);
            for (var issuer : acceptedIssuers) {
                tmpRolesByIssuer.put(issuer, acceptedRoles);
            }
        });
        return Map.copyOf(tmpRolesByIssuer);
    }

    @Bean
    public JwtAuthenticationConverterFactory jwtAuthenticationConverterFactory(@Value("${config.rest.security.principal-claim}") String principalClaim) {
        return new JwtAuthenticationConverterFactory(jwtProviderProperties.getProviders(), principalClaim, jwtProviderUtils);
    }

    @Bean
    public IssuerToDecoderMapFactory issuerToDecoderMapFactory() {
        return new IssuerToDecoderMapFactory(jwtProviderUtils);
    }

    @Bean
    public TokenDecoderFactory tokenDecoderFactory(IssuerToDecoderMapFactory issuerToDecoderMapFactory) {
        return new TokenDecoderFactoryImpl(jwtProviderProperties.getProviders(), issuerToDecoderMapFactory);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   TokenDecoderFactory tokenDecoderFactory,
                                                   JwtAuthenticationConverterFactory jwtAuthenticationConverterFactory,
                                                   Map<String, Set<String>> allowedRolesByIssuer) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(publicPathPatterns()).permitAll()
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().denyAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
                        .jwt(jwt -> jwt.decoder(tokenDecoderFactory.createJwtDecoder())
                                .jwtAuthenticationConverter(token -> {
                                    var issuer = token.getIssuer().toString();
                                    var converter = jwtAuthenticationConverterFactory.getConverter(issuer);
                                    var authenticationToken = converter.convert(token);
                                    var allowedRolesForIssuer = allowedRolesByIssuer.getOrDefault(issuer, Set.of());
                                    var filtered = authenticationToken.getAuthorities().stream()
                                            .map(GrantedAuthority::getAuthority)
                                            .filter(allowedRolesForIssuer::contains)
                                            .map(SimpleGrantedAuthority::new)
                                            .toList();
                                    log.trace("Authorization state - token: {}, issuer: {}, authenticationToken: {},allowedRolesForIssuer: {}, authorities: {}", token, issuer, authenticationToken, allowedRolesForIssuer, authenticationToken.getAuthorities());
                                    if (filtered.isEmpty()) {
                                        log.warn("Access denied for issuer:{}. No allowed roles for user {}", issuer, authenticationToken.getName());
                                        return new JwtAuthenticationToken(token);
                                    }
                                    return new JwtAuthenticationToken(token, filtered, authenticationToken.getName());
                                })));
        return http.build();
    }

    protected String[] publicPathPatterns() {
        var swaggerPaths = disableSwaggerAuthorization
                ? List.of("/swagger-ui/**", "/v3/api-docs/**")
                : List.<String>of();
        var appHealthPaths = List.of("/api/v1/health/**");

        return Stream.of(
                        swaggerPaths,
                        appHealthPaths
                )
                .flatMap(Collection::stream)
                .toArray(String[]::new);
    }
}