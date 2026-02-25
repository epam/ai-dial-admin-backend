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
public class OpaqueTokenProviderConfig {

    public static final String IDP_CLAIM = "idp";

    private String name;
    private String userInfoEndpoint;
    private String principalClaim;
    private List<String> roleClaims;
    private Set<String> allowedRoles;
    private List<String> emailClaims;

    public static OpaqueTokenProviderConfig from(String name, IdentityProvidersProperties.ProviderConfig config) {
        return OpaqueTokenProviderConfig.builder()
                .name(name)
                .userInfoEndpoint(config.getUserInfoEndpoint())
                .principalClaim(config.getPrincipalClaim())
                .roleClaims(config.getRoleClaims())
                .emailClaims(config.getEmailClaims())
                .allowedRoles(config.getAllowedRoles())
                .build();
    }
}