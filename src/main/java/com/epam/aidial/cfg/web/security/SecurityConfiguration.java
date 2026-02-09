package com.epam.aidial.cfg.web.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.HashMap;
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

    @Value("${config.rest.security.disable-swagger-authorization}")
    protected boolean disableSwaggerAuthorization;

    @Bean
    public Map<String, JwtProviderSetup> jwtProviderSetupByIssuer(
            JwtProvidersProperties jwtProviderProperties,
            NimbusJwtDecoderResolver nimbusJwtDecoderResolver,
            @Value("${config.rest.security.default.allowedRoles}") Set<String> defaultAllowedRoles,
            @Value("${config.rest.security.principal-claim}") String principalClaim) {
        JwtProviderUtils jwtProviderUtils = new JwtProviderUtils();
        JwtDecoderFactory jwtDecoderFactory = new JwtDecoderFactory();
        JwtAuthenticationConverterFactory jwtAuthenticationConverterFactory = new JwtAuthenticationConverterFactory();

        Map<String, JwtProviderSetup> result = new HashMap<>();

        for (var config : jwtProviderProperties.getProviders().values()) {
            var acceptedIssuers = jwtProviderUtils.getAcceptedIssuers(config);
            var acceptedAudiences = jwtProviderUtils.getAcceptedAudiences(config);
            var allowedRoles = jwtProviderUtils.getAllowedRoles(config, defaultAllowedRoles);

            var jwtDecoder = jwtDecoderFactory.createDecoder(acceptedIssuers, acceptedAudiences, config, nimbusJwtDecoderResolver);
            var jwtAuthenticationConverter = jwtAuthenticationConverterFactory.create(config.getRoleClaims(), principalClaim);

            var jwtProviderSetup = JwtProviderSetup.builder()
                    .allowedRoles(allowedRoles)
                    .jwtDecoder(jwtDecoder)
                    .jwtAuthenticationConverter(jwtAuthenticationConverter)
                    .build();

            for (var issuer : acceptedIssuers) {
                result.put(issuer, jwtProviderSetup);
            }
        }

        return result;
    }

    @Bean
    public NimbusJwtDecoderResolver nimbusJwtDecoderResolver() {
        return config -> NimbusJwtDecoder.withJwkSetUri(config.getJwkSetUri()).build();
    }

    @Bean
    public JwtDecoder jwtDecoder(Map<String, JwtProviderSetup> jwtProviderSetupByIssuer) {
        return new MultiIssuerJwtDecoder(jwtProviderSetupByIssuer);
    }

    @Bean
    public Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter(Map<String, JwtProviderSetup> jwtProviderSetupByIssuer) {
        return new MultiIssuerJwtAuthenticationConverter(jwtProviderSetupByIssuer);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   JwtDecoder jwtDecoder,
                                                   Converter<Jwt, AbstractAuthenticationToken> jwtAuthenticationConverter) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(publicPathPatterns()).permitAll()
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().denyAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
                        .jwt(jwt -> jwt.decoder(jwtDecoder).jwtAuthenticationConverter(jwtAuthenticationConverter)));
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