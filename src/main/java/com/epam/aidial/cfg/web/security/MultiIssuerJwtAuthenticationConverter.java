package com.epam.aidial.cfg.web.security;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimNames;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class MultiIssuerJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    private String principalClaimName = JwtClaimNames.SUB;
    private final Set<String> emailClaims = new LinkedHashSet<>();
    private final Set<String> allowedRoles;
    private final boolean requireEmail;

    public MultiIssuerJwtAuthenticationConverter(List<String> issuerEmailClaims, Set<String> allowedRoles, String defaultClaimsEmailKey,
                                                 boolean requireEmail) {
        this.allowedRoles = allowedRoles;
        this.requireEmail = requireEmail;

        if (!CollectionUtils.isEmpty(issuerEmailClaims)) {
            emailClaims.addAll(issuerEmailClaims);
        } else if (StringUtils.isNotBlank(defaultClaimsEmailKey)) {
            emailClaims.add(defaultClaimsEmailKey);
        }
    }

    @NotNull
    @Override
    public AbstractAuthenticationToken convert(@NotNull Jwt jwt) {
        var issuer = jwt.getIssuer().toString();
        var authorities = this.jwtGrantedAuthoritiesConverter.convert(jwt);
        var principalClaimValue = jwt.getClaimAsString(this.principalClaimName);
        var filtered = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(allowedRoles::contains)
                .map(SimpleGrantedAuthority::new)
                .toList();

        var email = IdentityProviderUtils.extractFirstClaim(jwt, List.copyOf(emailClaims));

        if (requireEmail && email.isEmpty()) {
            throw new IllegalStateException("Email claim is required");
        }

        var details = email.map(UserSecurityDetails::new)
                .orElseGet(() -> new UserSecurityDetails(null));

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

    public void setJwtGrantedAuthoritiesConverter(
            Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter) {
        Assert.notNull(jwtGrantedAuthoritiesConverter, "jwtGrantedAuthoritiesConverter cannot be null");
        this.jwtGrantedAuthoritiesConverter = jwtGrantedAuthoritiesConverter;
    }

    public void setPrincipalClaimName(String principalClaimName) {
        Assert.hasText(principalClaimName, "principalClaimName cannot be empty");
        this.principalClaimName = principalClaimName;
    }

}