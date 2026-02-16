package com.epam.aidial.cfg.web.security;

import com.epam.aidial.cfg.utils.IdentityProviderTestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtAuthenticationConverterFactoryTest {
    private static final String TEST_ISSUER = "https://sts.windows.net/issuer_test/";
    private final IdentityProviderUtils identityProviderUtils = new IdentityProviderUtils();
    private JwtAuthenticationConverterFactory factory;
    private ConfigurableJwtAuthenticationConverter converter;

    @BeforeEach
    void setup() {
        factory = new JwtAuthenticationConverterFactory(
                List.of(JwtProviderConfig.from("test", IdentityProviderTestHelper.createJwtProviderConfig())),
                "testPrincipal",
                identityProviderUtils,
                Set.of("admin", "ConfigAdmin"),
                "unique_name", false);
        converter = factory.getConverter(TEST_ISSUER);
    }

    @Test
    void whenNoProviders_thenThrows() {
        var jwtToken = generateTestToken(
                Map.of(
                        "iss", TEST_ISSUER,
                        "allowedRoles", List.of("TEST")
                )
        );
        var authenticationToken = converter.convert(jwtToken);
        var authorities = authoritiesToStrings(authenticationToken.getAuthorities());
        assertThat(authorities).hasSize(0);
    }

    @Test
    void whenRoleAllowed_thenAuthoritiesNotEmpty() {
        var jwtToken = generateTestToken(
                Map.of(
                        "iss", TEST_ISSUER,
                        "roles", List.of("USER")
                )
        );
        var authenticationToken = converter.convert(jwtToken);
        var authorities = authoritiesToStrings(authenticationToken.getAuthorities());
        assertThat(authorities).hasSize(1);
    }

    @Test
    void whenEmailPresentInEmailClaims_thenEmailInDetails() {
        var jwtToken = generateTestToken(
                Map.of(
                        "iss", TEST_ISSUER,
                        "roles", List.of("USER"),
                        "unique_name", "default@test.com",
                        "email", "test@test.com"
                )
        );
        var authenticationToken = converter.convert(jwtToken);
        var details = (UserSecurityDetails) authenticationToken.getDetails();
        assertThat(details.email()).isEqualTo("test@test.com");
    }

    @Test
    void whenEmailPresentInDefaultEmailClaimsButNotInIssuerEmailClaims_thenEmailInNull() {
        var jwtToken = generateTestToken(
                Map.of(
                        "iss", TEST_ISSUER,
                        "roles", List.of("USER"),
                        "unique_name", "default@test.com"
                )
        );
        var authenticationToken = converter.convert(jwtToken);
        var details = (UserSecurityDetails) authenticationToken.getDetails();
        assertThat(details.email()).isNull();
    }

    @Test
    void whenEmailNotPresent_thenEmailIsNull() {
        var jwtToken = generateTestToken(
                Map.of(
                        "iss", TEST_ISSUER,
                        "roles", List.of("USER")
                )
        );
        var authenticationToken = converter.convert(jwtToken);
        var details = (UserSecurityDetails) authenticationToken.getDetails();
        assertThat(details.email()).isEqualTo(null);
    }

    @Test
    void whenEmailNotPresentAndRequired_thenThrow() {
        var factoryWithRequiredEmail = new JwtAuthenticationConverterFactory(
                List.of(JwtProviderConfig.from("test", IdentityProviderTestHelper.createJwtProviderConfig())),
                "testPrincipal",
                identityProviderUtils,
                Set.of("admin", "ConfigAdmin"),
                "unique_name", true);
        var converterWithRequiredEmail = factoryWithRequiredEmail.getConverter(TEST_ISSUER);
        var jwtToken = generateTestToken(
                Map.of(
                        "iss", TEST_ISSUER,
                        "roles", List.of("USER")
                )
        );
        assertThrows(IllegalStateException.class, () -> converterWithRequiredEmail.convert(jwtToken));
    }

    private static List<String> authoritiesToStrings(Collection<? extends GrantedAuthority> auths) {
        return auths.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList());
    }

    private static Jwt generateTestToken(Map<String, Object> claims) {
        return new Jwt(
                "tokenValue",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Map.of("alg", "HS256", "typ", "JWT"),
                claims
        );
    }
}