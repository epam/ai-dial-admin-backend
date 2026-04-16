package com.epam.aidial.cfg.domain.validator;

import com.epam.aidial.cfg.domain.model.AuthenticationType;
import com.epam.aidial.cfg.domain.model.ResourceAuthSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ResourceAuthSettingsValidatorTest {

    private ResourceAuthSettingsValidator validator;

    private static final String DOMAIN_OBJECT_TYPE = "DomainObjectType";
    private static final String ID = "object-123";

    @BeforeEach
    void setUp() {
        validator = new ResourceAuthSettingsValidator();
    }

    @Test
    void validate_shouldNotThrowExceptionWhenResourceSettingsIsNull() {
        // when & then
        assertThatNoException()
                .isThrownBy(() -> validator.validate(null, DOMAIN_OBJECT_TYPE, ID));
    }

    @Test
    void validate_shouldNotThrowExceptionWhenNoneAuthType() {
        // given
        ResourceAuthSettings settings = new ResourceAuthSettings();
        settings.setAuthenticationType(AuthenticationType.NONE);

        // when & then
        assertThatNoException()
                .isThrownBy(() -> validator.validate(settings, DOMAIN_OBJECT_TYPE, ID));
    }

    @Test
    void validate_shouldNotThrowExceptionWhenOauthAuthTypeAndAllFieldsAreValid() {
        ResourceAuthSettings settings = buildValidOauthSettings();

        assertThatNoException()
                .isThrownBy(() -> validator.validate(settings, DOMAIN_OBJECT_TYPE, ID));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void validate_shouldThrowExceptionWhenOauthAuthTypeAndClientIdIsBlank(String clientId) {
        // given
        ResourceAuthSettings settings = buildValidOauthSettings();
        settings.setClientId(clientId);

        // when & then
        assertThatThrownBy(() -> validator.validate(settings, DOMAIN_OBJECT_TYPE, ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Client id")
                .hasMessageContaining("must not be blank for " + DOMAIN_OBJECT_TYPE + " with id:'" + ID + "'");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void validate_shouldThrowExceptionWhenOauthAuthTypeAndClientSecretIsBlank(String clientSecret) {
        // given
        ResourceAuthSettings settings = buildValidOauthSettings();
        settings.setClientSecret(clientSecret);

        // when & then
        assertThatThrownBy(() -> validator.validate(settings, DOMAIN_OBJECT_TYPE, ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Client secret")
                .hasMessageContaining("must not be blank for " + DOMAIN_OBJECT_TYPE + " with id:'" + ID + "'");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void validate_shouldThrowExceptionWhenOauthAuthTypeAndAuthorizationEndpointIsBlank(String authorizationEndpoint) {
        // given
        ResourceAuthSettings settings = buildValidOauthSettings();
        settings.setAuthorizationEndpoint(authorizationEndpoint);

        // when & then
        assertThatThrownBy(() -> validator.validate(settings, DOMAIN_OBJECT_TYPE, ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Authorization endpoint")
                .hasMessageContaining("must not be blank for " + DOMAIN_OBJECT_TYPE + " with id:'" + ID + "'");
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void validate_shouldThrowExceptionWhenOauthAuthTypeAndTokenEndpointIsBlank(String tokenEndpoint) {
        // given
        ResourceAuthSettings settings = buildValidOauthSettings();
        settings.setTokenEndpoint(tokenEndpoint);

        // when & then
        assertThatThrownBy(() -> validator.validate(settings, DOMAIN_OBJECT_TYPE, ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Token endpoint")
                .hasMessageContaining("must not be blank for " + DOMAIN_OBJECT_TYPE + " with id:'" + ID + "'");
    }

    @Test
    void validate_shouldNotThrowExceptionWhenApiKeyAuthTypeAndApiKeyHeaderIsValid() {
        // given
        ResourceAuthSettings settings = new ResourceAuthSettings();
        settings.setAuthenticationType(AuthenticationType.API_KEY);
        settings.setApiKeyHeader("X-Api-Key");

        // when & then
        assertThatNoException()
                .isThrownBy(() -> validator.validate(settings, DOMAIN_OBJECT_TYPE, ID));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"  ", "\t", "\n"})
    void validate_shouldThrowExceptionWhenApiKeyAuthTypeAndApiKeyHeaderIsBlank(String apiKeyHeader) {
        ResourceAuthSettings settings = new ResourceAuthSettings();
        settings.setAuthenticationType(AuthenticationType.API_KEY);
        settings.setApiKeyHeader(apiKeyHeader);

        assertThatThrownBy(() -> validator.validate(settings, DOMAIN_OBJECT_TYPE, ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("API Key header")
                .hasMessageContaining("must not be blank for " + DOMAIN_OBJECT_TYPE + " with id:'" + ID + "'");
    }

    private ResourceAuthSettings buildValidOauthSettings() {
        ResourceAuthSettings settings = new ResourceAuthSettings();
        settings.setAuthenticationType(AuthenticationType.OAUTH);
        settings.setClientId("valid-client-id");
        settings.setClientSecret("valid-client-secret");
        settings.setAuthorizationEndpoint("https://auth.example.com/authorize");
        settings.setTokenEndpoint("https://auth.example.com/token");
        return settings;
    }
}