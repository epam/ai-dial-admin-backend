package com.epam.aidial.cfg.web.security;

import com.epam.aidial.cfg.utils.ProviderTestHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtProvidersPropertiesTest {

    @Test
    void whenNoProviders_thenThrows() {
        JwtProvidersProperties properties = new JwtProvidersProperties();
        properties.getProviders().clear();
        assertThrows(IllegalStateException.class, properties::checkProviders);
    }

    @Test
    void whenProvidersPresentAndIssueIsBlank_thenThrows() {
        JwtProvidersProperties properties = new JwtProvidersProperties();
        JwtProvidersProperties.ProviderConfig config = ProviderTestHelper.createProviderConfig();
        config.setIssuer("");
        properties.getProviders().put("test", config);
        assertThrows(IllegalStateException.class, properties::checkProviders);
    }

    @Test
    void whenProvidersPresentAndUriIsBlank_thenThrows() {
        JwtProvidersProperties properties = new JwtProvidersProperties();
        JwtProvidersProperties.ProviderConfig config = ProviderTestHelper.createProviderConfig();
        config.setJwkSetUri("");
        properties.getProviders().put("test", config);
        assertThrows(IllegalStateException.class, properties::checkProviders);
    }

    @Test
    void whenProvidersPresentAndUriIsNull_thenNoException() {
        JwtProvidersProperties properties = new JwtProvidersProperties();
        properties.getProviders().put("test", ProviderTestHelper.createProviderConfig());
        assertDoesNotThrow(properties::checkProviders);
    }
}