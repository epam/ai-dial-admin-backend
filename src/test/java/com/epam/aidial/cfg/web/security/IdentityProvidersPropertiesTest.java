package com.epam.aidial.cfg.web.security;

import com.epam.aidial.cfg.utils.IdentityProviderTestHelper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdentityProvidersPropertiesTest {

    @Test
    void whenNoProviders_thenThrows() {
        IdentityProvidersProperties properties = new IdentityProvidersProperties();
        properties.getProviders().clear();
        assertThrows(IllegalStateException.class, properties::checkProviders);
    }

    @Test
    void whenProvidersPresentAndIssuerIsBlankForJwtProvider_thenThrows() {
        IdentityProvidersProperties properties = new IdentityProvidersProperties();
        IdentityProvidersProperties.ProviderConfig config = IdentityProviderTestHelper.createJwtProviderConfig();
        config.setIssuer("");
        properties.getProviders().put("test", config);
        assertThrows(IllegalStateException.class, properties::checkProviders);
    }

    @Test
    void whenProvidersPresentAndJwkSetUriAndUserInfoEndpointAreBlank_thenThrows() {
        IdentityProvidersProperties properties = new IdentityProvidersProperties();
        IdentityProvidersProperties.ProviderConfig config = IdentityProviderTestHelper.createJwtProviderConfig();
        config.setJwkSetUri("");
        properties.getProviders().put("test", config);
        assertThrows(IllegalStateException.class, properties::checkProviders);
    }

    @Test
    void whenProvidersPresentAndJwkSetUriAndUserInfoEndpointAreNotBlank_thenThrows() {
        IdentityProvidersProperties properties = new IdentityProvidersProperties();
        IdentityProvidersProperties.ProviderConfig config = IdentityProviderTestHelper.createJwtProviderConfig();
        config.setUserInfoEndpoint("https://test/userinfo");
        properties.getProviders().put("test", config);
        assertThrows(IllegalStateException.class, properties::checkProviders);
    }

    @Test
    void whenProvidersPresentAndJwkSetUriAndIssuerAreNotBlank_thenNoException() {
        IdentityProvidersProperties properties = new IdentityProvidersProperties();
        properties.getProviders().put("test", IdentityProviderTestHelper.createJwtProviderConfig());
        assertDoesNotThrow(properties::checkProviders);
    }

    @Test
    void whenProvidersPresentAndUserInfoEndpointIsNotBlank_thenNoException() {
        IdentityProvidersProperties properties = new IdentityProvidersProperties();
        properties.getProviders().put("test", IdentityProviderTestHelper.createOpaqueTokenProviderConfig());
        assertDoesNotThrow(properties::checkProviders);
    }

    @Test
    void whenProvidersPresentAndRoleClaimsAreEmpty_thenThrows() {
        IdentityProvidersProperties properties = new IdentityProvidersProperties();
        IdentityProvidersProperties.ProviderConfig config = IdentityProviderTestHelper.createJwtProviderConfig();
        config.setRoleClaims(null);
        properties.getProviders().put("test", config);
        assertThrows(IllegalStateException.class, properties::checkProviders);
    }
}