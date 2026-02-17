package com.epam.aidial.cfg.web.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class ConfigurableJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    private final String principalClaimName;
    private final Set<String> emailClaims = new LinkedHashSet<>();
    private final Set<String> allowedRoles;
    private final boolean requireEmail;
    private final IdentityProviderUtils identityProviderUtils;

    public ConfigurableJwtAuthenticationConverter(List<String> issuerEmailClaims,
                                                  Set<String> allowedRoles,
                                                  String defaultClaimsEmailKey,
                                                  boolean requireEmail,
                                                  IdentityProviderUtils identityProviderUtils,
                                                  Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter,
                                                  String principalClaimName) {
        this.allowedRoles = allowedRoles;
        this.requireEmail = requireEmail;
        this.identityProviderUtils = identityProviderUtils;
        this.principalClaimName =
                StringUtils.defaultIfBlank(principalClaimName, JwtClaimNames.SUB);
        this.jwtGrantedAuthoritiesConverter = jwtGrantedAuthoritiesConverter;
        if (!CollectionUtils.isEmpty(issuerEmailClaims)) {
            this.emailClaims.addAll(issuerEmailClaims);
        } else if (StringUtils.isNotBlank(defaultClaimsEmailKey)) {
            this.emailClaims.add(defaultClaimsEmailKey);
        }
    }

    @NotNull
    @Override
    public AbstractAuthenticationToken convert(@NotNull Jwt jwt) {
        var email = identityProviderUtils.extractFirstClaim(jwt.getClaims(), List.copyOf(emailClaims));

        if (requireEmail && email.isEmpty()) {
            throw new AuthenticationServiceException("Email claim is required");
        }

        var details = email.map(UserSecurityDetails::new)
                .orElseGet(() -> new UserSecurityDetails(null));
        var issuer = jwt.getIssuer().toString();
        var authorities = this.jwtGrantedAuthoritiesConverter.convert(jwt);
        var principalClaimValue = jwt.getClaimAsString(this.principalClaimName);
        var filtered = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(allowedRoles::contains)
                .map(SimpleGrantedAuthority::new)
                .toList();

        JwtAuthenticationToken authToken =
                filtered.isEmpty()
                        ? new JwtAuthenticationToken(jwt)
                        : new JwtAuthenticationToken(jwt, filtered, principalClaimValue);

        if (filtered.isEmpty()) {
            log.warn("Authorization failed - issuer: {}, allowedRolesForIssuer: {}, authorities: {}",
                    issuer, allowedRoles, authorities);
        }

        authToken.setDetails(details);
        log.trace("Authorization state - token: {}, issuer: {}, authenticationToken: {},allowedRolesForIssuer: {}, authorities: {}",
                jwt, issuer, authToken, allowedRoles, authorities);

        return authToken;
    }

}