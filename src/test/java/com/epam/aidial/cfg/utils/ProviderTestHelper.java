package com.epam.aidial.cfg.utils;

import com.epam.aidial.cfg.web.security.JwtProvidersProperties;

import java.util.List;

public class ProviderTestHelper {
    public static JwtProvidersProperties.ProviderConfig createProviderConfig() {
        var config = new JwtProvidersProperties.ProviderConfig();
        config.setIssuer("https://sts.windows.net/issuer_test/");
        config.setJwkSetUri("https://test/keys");
        config.setAudiences(List.of("audience_test"));
        config.setRoleClaims("roles");
        return config;
    }
}