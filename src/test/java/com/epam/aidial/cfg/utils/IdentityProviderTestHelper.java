package com.epam.aidial.cfg.utils;

import com.epam.aidial.cfg.web.security.IdentityProvidersProperties;

import java.util.List;
import java.util.Set;

public class IdentityProviderTestHelper {

    public static IdentityProvidersProperties.ProviderConfig createJwtProviderConfig() {
        var config = new IdentityProvidersProperties.ProviderConfig();
        config.setIssuer("https://sts.windows.net/issuer_test/");
        config.setJwkSetUri("https://test/keys");
        config.setAudiences(List.of("audience_test"));
        config.setRoleClaims(List.of("roles", "resource_access.roles"));
        config.setAllowedRoles(Set.of("testRole", "USER"));
        config.setEmailClaims(List.of("email"));
        return config;
    }

    public static IdentityProvidersProperties.ProviderConfig createOpaqueTokenProviderConfig() {
        var config = new IdentityProvidersProperties.ProviderConfig();
        config.setUserInfoEndpoint("https://test/userinfo");
        config.setRoleClaims(List.of("roles", "resource_access.roles"));
        return config;
    }
}