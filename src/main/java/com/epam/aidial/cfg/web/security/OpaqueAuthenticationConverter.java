package com.epam.aidial.cfg.web.security;

import com.epam.aidial.cfg.utils.MapExtractionUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.introspection.OpaqueTokenAuthenticationConverter;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class OpaqueAuthenticationConverter implements OpaqueTokenAuthenticationConverter {

    private final Set<String> emailClaims;
    private final Map<String, Set<AdminRole>> effectiveRoleMappings;
    private final boolean requireEmail;

    @Override
    public Authentication convert(String introspectedToken, OAuth2AuthenticatedPrincipal authenticatedPrincipal) {
        var providerName = (String) authenticatedPrincipal.getAttribute(OpaqueTokenProviderConfig.IDP_CLAIM);

        var accessToken = new OAuth2AccessToken(OAuth2AccessToken.TokenType.BEARER, introspectedToken, null, null);

        var email = MapExtractionUtils.extractFirstNonNullValue(authenticatedPrincipal.getAttributes(), emailClaims);

        if (requireEmail && email.isEmpty()) {
            throw new AuthenticationServiceException("Email claim is required");
        }

        var details = email.map(Object::toString)
                .map(UserSecurityDetails::new)
                .orElseGet(() -> new UserSecurityDetails(null));

        var authorities = authenticatedPrincipal.getAuthorities();
        var matchedIdpRoles = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .filter(effectiveRoleMappings::containsKey)
                .collect(Collectors.toSet());
        var adminRoles = IdentityProviderUtils.resolveAdminRoles(effectiveRoleMappings, matchedIdpRoles);
        var filtered = adminRoles.stream()
                .map(role -> new SimpleGrantedAuthority(role.name()))
                .toList();

        log.trace("Authorization state - token: {}, idp: {}, allowedRoles: {}, authorities: {}",
                introspectedToken, providerName, effectiveRoleMappings.keySet(), authenticatedPrincipal.getAuthorities());

        BearerTokenAuthentication authentication = new BearerTokenAuthentication(authenticatedPrincipal, accessToken, filtered);

        authentication.setDetails(details);

        if (filtered.isEmpty()) {
            log.warn("Access denied for idp: {}. No allowed roles for user {}", providerName, authenticatedPrincipal.getName());
            authentication.setAuthenticated(false);
        }
        return authentication;
    }
}
