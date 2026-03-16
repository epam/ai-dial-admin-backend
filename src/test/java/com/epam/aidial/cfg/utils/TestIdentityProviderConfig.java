package com.epam.aidial.cfg.utils;

import com.epam.aidial.cfg.web.security.IdentityProvidersProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.Set;

@Configuration
public class TestIdentityProviderConfig {

    @Primary
    @Bean
    public IdentityProvidersProperties identityProvidersProperties(ObjectMapper objectMapper) {
        var config = IdentityProviderTestHelper.createJwtProviderConfig();
        config.setAllowedRoles(Set.of("testRole"));

        var config2 = IdentityProviderTestHelper.createJwtProviderConfig();
        config2.setIssuer("https://sts.windows.net/issuer_test2/");
        config2.setJwkSetUri("https://test2/keys");
        config2.setAllowedRoles(Set.of());
        config2.setRolesMapping("{\"role1\":[\"FULL_ADMIN\"],\"role2\":[\"READ_ONLY_ADMIN\"]}");

        IdentityProvidersProperties properties = new IdentityProvidersProperties(objectMapper);
        properties.getProviders().put("test", config);
        properties.getProviders().put("test2", config2);

        return properties;
    }
}