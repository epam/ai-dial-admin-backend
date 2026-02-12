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
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationProvider;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
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

    @Value("${config.rest.security.default.allowedRoles}")
    protected Set<String> defaultAllowedRoles;

    @Value("${config.rest.security.disable-swagger-authorization}")
    protected boolean disableSwaggerAuthorization;

    @Bean
    public Map<String, Set<String>> allowedRolesByIssuer() {
        Map<String, Set<String>> tmpRolesByIssuer = new HashMap<>();
        var providers = identityProvidersProperties.getJwtProviders();
        providers.forEach(config -> {
            Set<String> acceptedRoles = new HashSet<>(defaultAllowedRoles);
            if (config.getAllowedRoles() != null) {
                acceptedRoles.addAll(config.getAllowedRoles());
            }
            var acceptedIssuers = identityProviderUtils.getAcceptedIssuers(config);
            for (var issuer : acceptedIssuers) {
                tmpRolesByIssuer.put(issuer, acceptedRoles);
            }
        });
        return Map.copyOf(tmpRolesByIssuer);
    }

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
    public JwtAuthenticationConverterFactory jwtAuthenticationConverterFactory(@Value("${config.rest.security.principal-claim}") String principalClaim) {
        return new JwtAuthenticationConverterFactory(identityProvidersProperties.getJwtProviders(), principalClaim, identityProviderUtils);
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
            Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter) {
        JwtDecoder jwtDecoder = tokenDecoderFactory.createJwtDecoder();
        JwtAuthenticationProvider jwtAuthenticationProvider = new JwtAuthenticationProvider(jwtDecoder);
        jwtAuthenticationProvider.setJwtAuthenticationConverter(jwtAuthenticationConverter);
        return jwtAuthenticationProvider;
    }

    @Bean
    public Converter<Jwt, ? extends AbstractAuthenticationToken> jwtAuthenticationConverter(
            JwtAuthenticationConverterFactory jwtAuthenticationConverterFactory,
            Map<String, Set<String>> allowedRolesByIssuer) {
        return token -> {
            var issuer = token.getIssuer().toString();
            var converter = jwtAuthenticationConverterFactory.getConverter(issuer);
            var authenticationToken = converter.convert(token);
            var allowedRolesForIssuer = allowedRolesByIssuer.getOrDefault(issuer, Set.of());
            var filtered = authenticationToken.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(allowedRolesForIssuer::contains)
                    .map(SimpleGrantedAuthority::new)
                    .toList();
            log.trace("Authorization state - token: {}, issuer: {}, authenticationToken: {},allowedRolesForIssuer: {}, authorities: {}",
                    token, issuer, authenticationToken, allowedRolesForIssuer, authenticationToken.getAuthorities());
            if (filtered.isEmpty()) {
                log.warn("Access denied for issuer:{}. No allowed roles for user {}", issuer, authenticationToken.getName());
                return new JwtAuthenticationToken(token);
            }
            return new JwtAuthenticationToken(token, filtered, authenticationToken.getName());
        };
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