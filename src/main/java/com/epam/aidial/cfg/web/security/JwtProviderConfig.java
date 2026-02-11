package com.epam.aidial.cfg.web.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
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
    private Set<String> allowedRoles;

    public static JwtProviderConfig from(String name, IdentityProvidersProperties.ProviderConfig config) {
        return JwtProviderConfig.builder()
                .name(name)
                .issuer(config.getIssuer())
                .jwkSetUri(config.getJwkSetUri())
                .principalClaim(config.getPrincipalClaim())
                .audiences(config.getAudiences())
                .aliases(config.getAliases())
                .roleClaims(config.getRoleClaims())
                .allowedRoles(config.getAllowedRoles())
                .build();
    }
}
