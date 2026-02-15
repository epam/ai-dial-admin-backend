package com.epam.aidial.cfg.web.security;

import com.epam.aidial.cfg.utils.SecretUtils;
import com.nimbusds.jwt.SignedJWT;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationManagerResolver;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.OpaqueTokenAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenIntrospector;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;

import java.text.ParseException;
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

    private final IdentityProvidersProperties identityProvidersProperties;
    private final IdentityProviderUtils identityProviderUtils;

    @Value("${config.rest.security.disable-swagger-authorization}")
    protected boolean disableSwaggerAuthorization;

    @Value("${config.rest.security.default.allowedRoles}")
    protected Set<String> defaultAllowedRoles;

    @Bean
    public Map<String, Set<String>> allowedRolesByOpaqueProviderName() {
        Map<String, Set<String>> tmpRolesByProviderName = new HashMap<>();
        var providers = identityProvidersProperties.getOpaqueTokenProviders();
        providers.forEach(config -> {
            Set<String> acceptedRoles = new HashSet<>(defaultAllowedRoles);
            if (config.getAllowedRoles() != null) {
                acceptedRoles.addAll(config.getAllowedRoles());
            }
            tmpRolesByProviderName.put(config.getName(), acceptedRoles);
        });
        return Map.copyOf(tmpRolesByProviderName);
    }

    @Bean
    public JwtAuthenticationConverterFactory jwtAuthenticationConverterFactory(@Value("${config.rest.security.principal-claim}") String principalClaim,
                                                                               @Value("${config.rest.security.default.email.claims}") String defaultClaimsEmailKey,
                                                                               @Value("${config.rest.security.default.allowedRoles}") Set<String> defaultAllowedRoles,
                                                                               @Value("${config.rest.security.require-email}") boolean requireEmail) {
        return new JwtAuthenticationConverterFactory(identityProvidersProperties.getJwtProviders(), principalClaim, identityProviderUtils,
                defaultAllowedRoles, defaultClaimsEmailKey,
                requireEmail);
    }

    @Bean
    public IssuerToDecoderMapFactory issuerToDecoderMapFactory() {
        return new IssuerToDecoderMapFactory(identityProviderUtils);
    }

    @Bean
    public TokenDecoderFactory tokenDecoderFactory(IssuerToDecoderMapFactory issuerToDecoderMapFactory) {
        return new TokenDecoderFactoryImpl(identityProvidersProperties.getJwtProviders(), issuerToDecoderMapFactory);
    }

    @Bean
    public TokenIntrospectorFactory tokenIntrospectorFactory() {
        return new TokenIntrospectorFactoryImpl(identityProvidersProperties.getOpaqueTokenProviders());
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(publicPathPatterns()).permitAll()
                        .requestMatchers("/api/v1/**").authenticated()
                        .anyRequest().denyAll())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(oauth2ResourceServer -> oauth2ResourceServer
                        .authenticationManagerResolver(authenticationManagerResolver));
        return http.build();
    }

    @Bean
    public AuthenticationManagerResolver<HttpServletRequest> authenticationManagerResolver(
            JwtAuthenticationProvider jwtAuthenticationProvider,
            OpaqueTokenAuthenticationProvider opaqueTokenAuthenticationProvider) {
        BearerTokenResolver tokenResolver = new DefaultBearerTokenResolver();

        AuthenticationManager jwtAuth = new ProviderManager(jwtAuthenticationProvider);
        AuthenticationManager opaqueAuth = new ProviderManager(opaqueTokenAuthenticationProvider);

        return request -> {
            String token = tokenResolver.resolve(request);
            try {
                if (log.isTraceEnabled()) {
                    log.trace("authManagerResolve. token: {}", token);
                } else if (log.isDebugEnabled()) {
                    log.debug("authManagerResolve. token: {}", SecretUtils.mask(token));
                }
                SignedJWT.parse(token);
                return jwtAuth;
            } catch (ParseException e) {
                log.debug("Failed to parse JWT token: {}. Falling back to opaque token auth", SecretUtils.mask(token));
                return opaqueAuth;
            }
        };
    }

    @Bean
    public JwtAuthenticationProvider jwtAuthenticationProvider(
            TokenDecoderFactory tokenDecoderFactory,
            JwtAuthenticationConverterFactory jwtAuthenticationConverterFactory) {
        JwtDecoder jwtDecoder = tokenDecoderFactory.createJwtDecoder();
        JwtAuthenticationProvider jwtAuthenticationProvider = new JwtAuthenticationProvider(jwtDecoder);
        jwtAuthenticationProvider.setJwtAuthenticationConverter(token -> {

            String issuer = token.getIssuer().toString();
            var converter = jwtAuthenticationConverterFactory.getConverter(issuer);
            return converter.convert(token);
        });
        return jwtAuthenticationProvider;
    }

    @Bean
    public OpaqueTokenAuthenticationProvider opaqueTokenAuthenticationProvider(
            TokenIntrospectorFactory tokenIntrospectorFactory,
            OpaqueTokenAuthenticationConverter opaqueTokenAuthenticationConverter) {
        OpaqueTokenIntrospector opaqueTokenIntrospector = tokenIntrospectorFactory.createOpaqueTokenIntrospector();
        OpaqueTokenAuthenticationProvider opaqueTokenAuthenticationProvider = new OpaqueTokenAuthenticationProvider(opaqueTokenIntrospector);
        opaqueTokenAuthenticationProvider.setAuthenticationConverter(opaqueTokenAuthenticationConverter);
        return opaqueTokenAuthenticationProvider;
    }

    @Bean
    public OpaqueTokenAuthenticationConverter opaqueTokenAuthenticationConverter(Map<String, Set<String>> allowedRolesByOpaqueProviderName) {
        return (introspectedToken, authenticatedPrincipal) -> {
            var providerName = (String) authenticatedPrincipal.getAttribute(OpaqueTokenProviderConfig.IDP_CLAIM);
            var accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, introspectedToken, null, null);

            var allowedRolesForProvider = allowedRolesByOpaqueProviderName.getOrDefault(providerName, Set.of());
            List<SimpleGrantedAuthority> filtered = authenticatedPrincipal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(allowedRolesForProvider::contains)
                    .map(SimpleGrantedAuthority::new)
                    .toList();
            log.trace("Authorization state - token: {}, idp: {}, allowedRoles: {}, authorities: {}",
                    introspectedToken, providerName, allowedRolesForProvider, authenticatedPrincipal.getAuthorities());

            BearerTokenAuthentication authentication = new BearerTokenAuthentication(authenticatedPrincipal, accessToken, filtered);

            if (filtered.isEmpty()) {
                log.warn("Access denied for idp: {}. No allowed roles for user {}", providerName, authenticatedPrincipal.getName());
                authentication.setAuthenticated(false);
            }

            return authentication;
        };
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