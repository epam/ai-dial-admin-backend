package com.epam.aidial.cfg.web.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;

@Configuration(proxyBeanMethods = false)
@EnableWebSecurity
@EnableMethodSecurity(securedEnabled = true, jsr250Enabled = true)
@ConditionalOnProperty(value = "config.rest.security.mode", havingValue = "oidc", matchIfMissing = true)
@RequiredArgsConstructor
public class SecurityConfiguration {

    private final JwtProvidersProperties jwtProviderProperties;
    private final ProviderUtils providerUtils;

    @Value("${config.rest.security.allowedRoles}")
    protected String[] allowedRoles;

    @Value("${config.rest.security.disable-swagger-authorization}")
    protected boolean disableSwaggerAuthorization;

    @Bean
    public JwtAuthenticationConverterFactory jwtAuthenticationConverterFactory(@Value("${config.rest.security.principal-claim}") String principalClaim) {
        return new JwtAuthenticationConverterFactory(jwtProviderProperties.getProviders(), principalClaim, providerUtils);
    }

    @Bean
    public IssuerToDecoderMapFactory issuerToDecoderMapFactory() {
        return new IssuerToDecoderMapFactory(providerUtils);
    }

    @Bean
    public TokenDecoderFactory tokenDecoderFactory(IssuerToDecoderMapFactory issuerToDecoderMapFactory) {
        return new TokenDecoderFactoryImpl(jwtProviderProperties.getProviders(), issuerToDecoderMapFactory);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   TokenDecoderFactory tokenDecoderFactory,
                                                   JwtAuthenticationConverterFactory jwtAuthenticationConverterFactory) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(publicPathPatterns()).permitAll()
                        .requestMatchers("/api/v1/**").hasAnyAuthority(allowedRoles)
                        .anyRequest().denyAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
                        .jwt(jwt -> jwt.decoder(tokenDecoderFactory.createJwtDecoder())
                                .jwtAuthenticationConverter(token -> {
                                    var issue = token.getIssuer().toString();
                                    var converter = jwtAuthenticationConverterFactory.getConverter(issue);
                                    return converter.convert(token);
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