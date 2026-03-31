package com.epam.aidial.cfg.web.security;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.DefaultOAuth2AuthenticatedPrincipal;
import org.springframework.security.oauth2.core.OAuth2AuthenticatedPrincipal;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class OpaqueAuthenticationConverterTest {

    @Mock
    private UserRolesResolver userRolesResolver;

    private OpaqueAuthenticationConverter converter;

    @BeforeEach
    void setUp() {
        converter = new OpaqueAuthenticationConverter(
                userRolesResolver,
                Set.of("email", "upn"),
                true
        );
    }

    @Test
    void convert_shouldThrowExceptionWhenEmailIsRequiredAndMissing() {
        // given
        OAuth2AuthenticatedPrincipal authenticatedPrincipal = new DefaultOAuth2AuthenticatedPrincipal(
                "user1",
                baseAttributes(Map.of(
                        OpaqueTokenProviderConfig.IDP_CLAIM, "provider"
                )),
                List.of()
        );

        // when & then
        Assertions.assertThatThrownBy(() -> converter.convert("token-value", authenticatedPrincipal))
                .isInstanceOf(AuthenticationServiceException.class)
                .hasMessage("Email claim is required");
    }

    @Test
    void convert_shouldSetDetailsAndReturnTokenWhenEmailPresent() {
        // given
        String email = "user@example.com";
        String name = "user1";
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("role"));

        OAuth2AuthenticatedPrincipal authenticatedPrincipal = new DefaultOAuth2AuthenticatedPrincipal(
                name,
                baseAttributes(Map.of(
                        "email", email,
                        OpaqueTokenProviderConfig.IDP_CLAIM, "provider"
                )),
                authorities
        );

        doReturn(authorities).when(userRolesResolver).resolve(argThat(collection ->
                collection != null && collection.size() == authorities.size() && collection.containsAll(authorities)
        ));

        // when
        Authentication authentication = converter.convert("token-value", authenticatedPrincipal);

        // then
        Assertions.assertThat(authentication.getAuthorities()).isEqualTo(authorities);
        Assertions.assertThat(authentication.getName()).isEqualTo(name);
        Assertions.assertThat(authentication.isAuthenticated()).isTrue();
        Assertions.assertThat(authentication.getDetails()).isInstanceOfSatisfying(
                UserSecurityDetails.class,
                userSecurityDetails -> assertThat(userSecurityDetails.email()).isEqualTo(email)
        );
    }

    @Test
    void convert_shouldSetAuthenticatedFalseWhenPrincipalIsReserved() {
        // given
        String email = "user@example.com";
        String name = "system";
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("role"));

        OAuth2AuthenticatedPrincipal authenticatedPrincipal = new DefaultOAuth2AuthenticatedPrincipal(
                name,
                baseAttributes(Map.of(
                        "email", email,
                        OpaqueTokenProviderConfig.IDP_CLAIM, "provider"
                )),
                authorities
        );

        doReturn(authorities).when(userRolesResolver).resolve(argThat(collection ->
                collection != null && collection.size() == authorities.size() && collection.containsAll(authorities)
        ));

        // when
        Authentication authentication = converter.convert("token-value", authenticatedPrincipal);

        // then
        Assertions.assertThat(authentication.getAuthorities()).isEqualTo(authorities);
        Assertions.assertThat(authentication.getName()).isEqualTo(name);
        Assertions.assertThat(authentication.isAuthenticated()).isFalse();
        Assertions.assertThat(authentication.getDetails()).isInstanceOfSatisfying(
                UserSecurityDetails.class,
                userSecurityDetails -> assertThat(userSecurityDetails.email()).isEqualTo(email)
        );
    }

    @Test
    void convert_shouldSetAuthenticatedFalseWhenUserRolesEmpty() {
        // given
        String email = "user@example.com";
        String name = "user1";
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("role"));

        OAuth2AuthenticatedPrincipal authenticatedPrincipal = new DefaultOAuth2AuthenticatedPrincipal(
                name,
                baseAttributes(Map.of(
                        "email", email,
                        OpaqueTokenProviderConfig.IDP_CLAIM, "provider"
                )),
                authorities
        );

        doReturn(List.of()).when(userRolesResolver).resolve(argThat(collection ->
                collection != null && collection.size() == authorities.size() && collection.containsAll(authorities)
        ));

        // when
        Authentication authentication = converter.convert("token-value", authenticatedPrincipal);

        // then
        Assertions.assertThat(authentication.getAuthorities()).isEmpty();
        Assertions.assertThat(authentication.getName()).isEqualTo(name);
        Assertions.assertThat(authentication.isAuthenticated()).isFalse();
        Assertions.assertThat(authentication.getDetails()).isInstanceOfSatisfying(
                UserSecurityDetails.class,
                userSecurityDetails -> assertThat(userSecurityDetails.email()).isEqualTo(email)
        );
    }

    private Map<String, Object> baseAttributes(Map<String, Object> custom) {
        Map<String, Object> attrs = new HashMap<>();
        attrs.put("sub", "user1");
        attrs.put("iss", "https://issuer");
        attrs.putAll(custom);
        return attrs;
    }
}