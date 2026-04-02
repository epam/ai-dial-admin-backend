package com.epam.aidial.cfg.web.security;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationConverterTest {

    private static final String PRINCIPAL_CLAIM = "sub";

    @Mock
    private Converter<Jwt, Collection<GrantedAuthority>> jwtGrantedAuthoritiesConverter;
    @Mock
    private UserRolesResolver userRolesResolver;

    private JwtAuthenticationConverter converter;

    @BeforeEach
    void setUp() {
        converter = new JwtAuthenticationConverter(
                jwtGrantedAuthoritiesConverter,
                userRolesResolver,
                PRINCIPAL_CLAIM,
                Set.of("email", "upn"),
                true
        );
    }

    @Test
    void convert_shouldThrowExceptionWhenEmailIsRequiredAndMissing() {
        // given
        Jwt jwt = createJwt(Map.of()); // No email claim

        // when & then
        Assertions.assertThatThrownBy(() -> converter.convert(jwt))
                .isInstanceOf(AuthenticationServiceException.class)
                .hasMessage("Email claim is required");
    }

    @Test
    void convert_shouldSetDetailsAndReturnTokenWhenEmailPresent() {
        // given
        String email = "user@example.com";
        Jwt jwt = createJwt(Map.of("email", email));

        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("role"));
        doReturn(authorities).when(jwtGrantedAuthoritiesConverter).convert(jwt);
        doReturn(authorities).when(userRolesResolver).resolve(authorities);

        // when
        JwtAuthenticationToken token = (JwtAuthenticationToken) converter.convert(jwt);

        // then
        Assertions.assertThat(token.getAuthorities()).isEqualTo(authorities);
        Assertions.assertThat(token.getName()).isEqualTo("user1");
        Assertions.assertThat(token.isAuthenticated()).isTrue();
        Assertions.assertThat(token.getDetails()).isInstanceOfSatisfying(
                UserSecurityDetails.class,
                userSecurityDetails -> assertThat(userSecurityDetails.email()).isEqualTo(email)
        );
    }

    @Test
    void convert_shouldSetAuthenticatedFalseWhenPrincipalIsReserved() {
        // given
        String email = "user@example.com";
        String principal = "system";
        Jwt jwt = createJwt(Map.of("email", email, PRINCIPAL_CLAIM, principal));

        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("role"));
        doReturn(authorities).when(jwtGrantedAuthoritiesConverter).convert(jwt);
        doReturn(authorities).when(userRolesResolver).resolve(authorities);

        // when
        JwtAuthenticationToken token = (JwtAuthenticationToken) converter.convert(jwt);

        // then
        Assertions.assertThat(token.getAuthorities()).isEqualTo(authorities);
        Assertions.assertThat(token.getName()).isEqualTo(principal);
        Assertions.assertThat(token.isAuthenticated()).isFalse();
        Assertions.assertThat(token.getDetails()).isInstanceOfSatisfying(
                UserSecurityDetails.class,
                userSecurityDetails -> assertThat(userSecurityDetails.email()).isEqualTo(email)
        );
    }

    @Test
    void convert_shouldSetAuthenticatedFalseWhenUserRolesEmpty() {
        // given
        String email = "user@example.com";
        Jwt jwt = createJwt(Map.of("email", email));

        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("role"));
        doReturn(authorities).when(jwtGrantedAuthoritiesConverter).convert(jwt);
        doReturn(List.of()).when(userRolesResolver).resolve(authorities);

        // when
        JwtAuthenticationToken token = (JwtAuthenticationToken) converter.convert(jwt);

        // then
        Assertions.assertThat(token.getAuthorities()).isEmpty();
        Assertions.assertThat(token.getName()).isEqualTo("user1");
        Assertions.assertThat(token.isAuthenticated()).isFalse();
        Assertions.assertThat(token.getDetails()).isInstanceOfSatisfying(
                UserSecurityDetails.class,
                userSecurityDetails -> assertThat(userSecurityDetails.email()).isEqualTo(email)
        );
    }

    private Jwt createJwt(Map<String, Object> customClaims) {
        Map<String, Object> baseClaims = new HashMap<>();
        baseClaims.put(PRINCIPAL_CLAIM, "user1");
        baseClaims.put("iss", "https://issuer");
        baseClaims.putAll(customClaims);
        return new Jwt(
                "token-value",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "RS256"),
                baseClaims
        );
    }
}