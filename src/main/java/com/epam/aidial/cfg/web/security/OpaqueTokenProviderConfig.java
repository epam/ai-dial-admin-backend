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
public class OpaqueTokenProviderConfig {

    public static final String IDP_CLAIM = "idp";

    private String name;
    private String userInfoEndpoint;
    private String principalClaim;
    private List<String> roleClaims;
    private List<String> emailClaims;
    private Map<String, Set<UserRole>> rolesMapping;

    public static OpaqueTokenProviderConfig from(String name,
                                                 IdentityProvidersProperties.ProviderConfig config,
                                                 Map<String, Set<UserRole>> rolesMapping) {
        return OpaqueTokenProviderConfig.builder()
                .name(name)
                .userInfoEndpoint(config.getUserInfoEndpoint())
                .principalClaim(config.getPrincipalClaim())
                .roleClaims(config.getRoleClaims())
                .emailClaims(config.getEmailClaims())
                .rolesMapping(rolesMapping)
                .build();
    }
}