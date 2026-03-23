package com.epam.aidial.cfg.web.security;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.utils.IdentityProviderTestHelper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IdentityProvidersPropertiesTest {

    private static final ObjectMapper OBJECT_MAPPER = JsonMapperConfiguration.createJsonMapper();

    private IdentityProvidersProperties properties;

    @BeforeEach
    void setUp() {
        properties = new IdentityProvidersProperties(OBJECT_MAPPER);
    }

    @Test
    void whenNoProviders_thenThrows() {
        properties.getProviders().clear();
        assertThrows(IllegalStateException.class, properties::checkProviders);
    }

    @Test
    void whenProvidersPresentAndIssuerIsBlankForJwtProvider_thenThrows() {
        IdentityProvidersProperties.ProviderConfig config = IdentityProviderTestHelper.createJwtProviderConfig();
        config.setIssuer("");
        properties.getProviders().put("test", config);
        assertThrows(IllegalStateException.class, properties::checkProviders);
    }

    @Test
    void whenProvidersPresentAndJwkSetUriAndUserInfoEndpointAreBlank_thenThrows() {
        IdentityProvidersProperties.ProviderConfig config = IdentityProviderTestHelper.createJwtProviderConfig();
        config.setJwkSetUri("");
        properties.getProviders().put("test", config);
        assertThrows(IllegalStateException.class, properties::checkProviders);
    }

    @Test
    void whenProvidersPresentAndJwkSetUriAndUserInfoEndpointAreNotBlank_thenThrows() {
        IdentityProvidersProperties.ProviderConfig config = IdentityProviderTestHelper.createJwtProviderConfig();
        config.setUserInfoEndpoint("https://test/userinfo");
        properties.getProviders().put("test", config);
        assertThrows(IllegalStateException.class, properties::checkProviders);
    }

    @Test
    void whenProvidersPresentAndJwkSetUriAndIssuerAreNotBlank_thenNoException() {
        properties.getProviders().put("test", IdentityProviderTestHelper.createJwtProviderConfig());
        assertDoesNotThrow(properties::checkProviders);
    }

    @Test
    void whenProvidersPresentAndUserInfoEndpointIsNotBlank_thenNoException() {
        properties.getProviders().put("test", IdentityProviderTestHelper.createOpaqueTokenProviderConfig());
        assertDoesNotThrow(properties::checkProviders);
    }

    @Test
    void whenProvidersPresentAndRoleClaimsAreEmpty_thenThrows() {
        IdentityProvidersProperties.ProviderConfig config = IdentityProviderTestHelper.createJwtProviderConfig();
        config.setRoleClaims(null);
        properties.getProviders().put("test", config);
        assertThrows(IllegalStateException.class, properties::checkProviders);
    }

    @Test
    void whenProvidersPresentAndRolesMappingIsInvalidJson_thenThrows() {
        IdentityProvidersProperties.ProviderConfig config = IdentityProviderTestHelper.createJwtProviderConfig();
        config.setRolesMapping("{");
        properties.getProviders().put("test", config);
        assertThrows(IllegalStateException.class, properties::checkProviders);
    }

    @Test
    void whenProvidersPresentAndRolesMappingContainsUnknownUserRole_thenThrows() {
        IdentityProvidersProperties.ProviderConfig config = IdentityProviderTestHelper.createJwtProviderConfig();
        config.setRolesMapping("{\"role1\":[\"UNKNOWN_ROLE\"]}");
        properties.getProviders().put("test", config);
        assertThrows(IllegalStateException.class, properties::checkProviders);
    }

    @Test
    void whenProvidersPresentAndRolesMappingIsValid_thenNoException() {
        IdentityProvidersProperties.ProviderConfig config = IdentityProviderTestHelper.createJwtProviderConfig();
        config.setRolesMapping("{\"role1\":[\"FULL_ADMIN\"],\"role2\":[\"READ_ONLY_ADMIN\"]}");
        properties.getProviders().put("test", config);
        assertDoesNotThrow(properties::checkProviders);
    }
}