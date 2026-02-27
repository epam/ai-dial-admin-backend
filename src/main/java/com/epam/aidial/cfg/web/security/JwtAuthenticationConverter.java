package com.epam.aidial.cfg.web.security;

import com.epam.aidial.cfg.utils.MapExtractionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter;
    private final String principalClaim;
    private final Set<String> emailClaims;
    private final Set<String> allowedRoles;
    private final boolean requireEmail;

    @NotNull
    @Override
    public AbstractAuthenticationToken convert(@NotNull Jwt jwt) {
        var email = MapExtractionUtils.extractFirstNonNullValue(jwt.getClaims(), emailClaims);

        if (requireEmail && email.isEmpty()) {
            throw new AuthenticationServiceException("Email claim is required");
        }

        var details = email.map(Object::toString)
                .map(UserSecurityDetails::new)
                .orElseGet(() -> new UserSecurityDetails(null));
        var issuer = jwt.getIssuer().toString();
        var authorities = jwtGrantedAuthoritiesConverter.convert(jwt);
        var principalClaimValue = jwt.getClaimAsString(principalClaim);
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