package com.epam.aidial.cfg.utils;

import com.epam.aidial.cfg.web.security.JwtProvidersProperties;

import java.util.List;

public class JwtProviderTestHelper {
    public static JwtProvidersProperties.ProviderConfig createProviderConfig() {
        var config = new JwtProvidersProperties.ProviderConfig();
        config.setIssuer("https://sts.windows.net/issuer_test/");
        config.setJwkSetUri("https://test/keys");
        config.setAudiences(List.of("audience_test"));
        config.setRoleClaims(List.of("roles", "resource_access.roles"));
        return config;
    }
}