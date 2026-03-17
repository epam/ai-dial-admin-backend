package com.epam.aidial.cfg.web.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JwtProviderConfig {

    private String name;
    private String issuer;
    private String jwkSetUri;
    private String principalClaim;
    private List<String> audiences;
    private List<String> aliases;
    private List<String> roleClaims;
    private List<String> emailClaims;
    private Set<String> allowedRoles;
    private Map<String, Set<UserRole>> rolesMapping;

    public static JwtProviderConfig from(String name,
                                         IdentityProvidersProperties.ProviderConfig config,
                                         Map<String, Set<UserRole>> rolesMapping) {
        return JwtProviderConfig.builder()
                .name(name)
                .issuer(config.getIssuer())
                .jwkSetUri(config.getJwkSetUri())
                .principalClaim(config.getPrincipalClaim())
                .audiences(config.getAudiences())
                .aliases(config.getAliases())
                .roleClaims(config.getRoleClaims())
                .emailClaims(config.getEmailClaims())
                .allowedRoles(config.getAllowedRoles())
                .rolesMapping(rolesMapping)
                .build();
    }
}