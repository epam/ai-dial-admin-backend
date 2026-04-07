package com.epam.aidial.cfg.web.security;

import com.epam.aidial.cfg.utils.MapExtractionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.Set;

@Slf4j
@RequiredArgsConstructor
public class JwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter;
    private final UserRolesResolver userRolesResolver;
    private final String principalClaim;
    private final Set<String> emailClaims;
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
        var userRoles = userRolesResolver.resolve(authorities);
        var principalClaimValue = jwt.getClaimAsString(principalClaim);

        if (userRoles.isEmpty()) {
            log.warn("Authorization failed - issuer: {}, authorities: {}, user roles: {}", issuer, authorities, userRoles);
            return new JwtAuthenticationToken(jwt);
        }

        JwtAuthenticationToken authToken = new JwtAuthenticationToken(jwt, userRoles, principalClaimValue);
        authToken.setDetails(details);

        log.trace("Authorization state - token: {}, issuer: {}, authenticationToken: {}, authorities: {}",
                jwt, issuer, authToken, authorities);

        return authToken;
    }

}