package com.epam.aidial.cfg.web.security;

import com.epam.aidial.cfg.utils.JwtProviderTestHelper;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class JwtAuthenticationConverterFactoryTest {
    private static final String TEST_ISSUER = "https://sts.windows.net/issuer_test/";
    private final JwtProviderUtils jwtProviderUtils = new JwtProviderUtils();

    @Test
    void whenNoProviders_thenThrows() {
        var factory = new JwtAuthenticationConverterFactory(
                Map.of("test", JwtProviderTestHelper.createProviderConfig()), "testPrincipal", jwtProviderUtils);
        var converter = factory.getConverter(TEST_ISSUER);
        var jwtToken = generateTestToken(
                Map.of(
                        "iss", TEST_ISSUER,
                        "roles", List.of("ADMIN", "USER")
                )
        );
        var authenticationToken = converter.convert(jwtToken);
        var authorities = authoritiesToStrings(authenticationToken.getAuthorities());
        assertThat(authorities).containsExactlyInAnyOrder("ADMIN", "USER");
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