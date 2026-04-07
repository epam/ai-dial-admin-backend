package com.epam.aidial.cfg.web.controller.oidc;

import com.epam.aidial.cfg.dto.AdapterDto;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.web.controller.AdaptersController;
import com.epam.aidial.cfg.web.facade.AdapterFacade;
import com.epam.aidial.cfg.web.security.UserSecurityDetails;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mockito;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdaptersController.class)
class AdaptersControllerSecurityTest extends AbstractControllerSecurityTest {

    private static final String MINIMAL_ADAPTER_JSON =
            "{\"name\":\"testAdapter\",\"displayName\":\"Test Adapter\",\"baseEndpoint\":\"http://localhost:8080\"}";

    @MockitoBean
    private AdapterFacade adapterFacade;

    @ParameterizedTest
    @MethodSource("invalidJwt")
    void testGetAllAdaptersShouldReturnUnauthorized(String jwtToken) throws Exception {
        // Given & When
        final var result = performGet("/api/v1/adapters", jwtToken);

        // Then
        result.andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @ParameterizedTest
    @MethodSource({"notAllowedRoles", "reservedPrincipal"})
    void testGetAllAdaptersShouldReturnForbidden(String jwtToken) throws Exception {
        // Given & When
        final var result = performGet("/api/v1/adapters", jwtToken);

        // Then
        result.andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @ParameterizedTest
    @MethodSource({"fullAdminRoles", "readOnlyAdminRoles"})
    void testGetAllAdaptersShouldReturnOk(String jwtToken,
                                          String expectedName,
                                          String expectedEmail,
                                          List<GrantedAuthority> expectedGrantedAuthorities) throws Exception {
        // Given & When
        final var result = performGet("/api/v1/adapters", jwtToken);

        // Then
        result
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(authenticated().withAuthentication(auth -> {
                    assertThat(auth).isNotNull();
                    assertThat(auth.getName()).isEqualTo(expectedName);
                    assertThat(auth.getAuthorities()).isEqualTo(expectedGrantedAuthorities);
                    assertThat(auth.getDetails()).isInstanceOfSatisfying(
                            UserSecurityDetails.class,
                            userSecurityDetails -> assertThat(userSecurityDetails.email()).isEqualTo(expectedEmail));
                }));
    }

    @ParameterizedTest
    @MethodSource("invalidJwt")
    void testGetAdapterShouldReturnUnauthorized(String jwtToken) throws Exception {
        // Given & When
        final var result = performGet("/api/v1/adapters/{adapterName}", jwtToken, "testAdapter");

        // Then
        result.andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @ParameterizedTest
    @MethodSource({"notAllowedRoles", "reservedPrincipal"})
    void testGetAdapterShouldReturnForbidden(String jwtToken) throws Exception {
        // Given & When
        final var result = performGet("/api/v1/adapters/{adapterName}", jwtToken, "testAdapter");

        // Then
        result.andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @ParameterizedTest
    @MethodSource({"fullAdminRoles", "readOnlyAdminRoles"})
    void testGetAdapterShouldReturnOk(String jwtToken,
                                      String expectedName,
                                      String expectedEmail,
                                      List<GrantedAuthority> expectedGrantedAuthorities) throws Exception {
        // Given
        Mockito.when(adapterFacade.getAdapterWithHash("testAdapter"))
                .thenReturn(new DtoWithDomainHash<>(new AdapterDto(), "hash"));

        // When
        final var result = performGet("/api/v1/adapters/{adapterName}", jwtToken, "testAdapter");

        // Then
        result
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(authenticated().withAuthentication(auth -> {
                    assertThat(auth).isNotNull();
                    assertThat(auth.getName()).isEqualTo(expectedName);
                    assertThat(auth.getAuthorities()).isEqualTo(expectedGrantedAuthorities);
                    assertThat(auth.getDetails()).isInstanceOfSatisfying(
                            UserSecurityDetails.class,
                            userSecurityDetails -> assertThat(userSecurityDetails.email()).isEqualTo(expectedEmail));
                }));
    }

    @ParameterizedTest
    @MethodSource("invalidJwt")
    void testCreateAdapterShouldReturnUnauthorized(String jwtToken) throws Exception {
        // Given & When
        final var result = performPost("/api/v1/adapters", jwtToken, MINIMAL_ADAPTER_JSON);

        // Then
        result.andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @ParameterizedTest
    @MethodSource({"notAllowedRoles", "readOnlyAdminRoles", "reservedPrincipal"})
    void testCreateAdapterShouldReturnForbidden(String jwtToken) throws Exception {
        // Given & When
        final var result = performPost("/api/v1/adapters", jwtToken, MINIMAL_ADAPTER_JSON);

        // Then
        result.andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @ParameterizedTest
    @MethodSource("fullAdminRoles")
    void testCreateAdapterShouldReturnNoContent(String jwtToken,
                                                String expectedName,
                                                String expectedEmail,
                                                List<GrantedAuthority> expectedGrantedAuthorities) throws Exception {
        // Given & When
        final var result = performPost("/api/v1/adapters", jwtToken, MINIMAL_ADAPTER_JSON);

        // Then
        result
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()))
                .andExpect(authenticated().withAuthentication(auth -> {
                    assertThat(auth).isNotNull();
                    assertThat(auth.getName()).isEqualTo(expectedName);
                    assertThat(auth.getAuthorities()).isEqualTo(expectedGrantedAuthorities);
                    assertThat(auth.getDetails()).isInstanceOfSatisfying(
                            UserSecurityDetails.class,
                            userSecurityDetails -> assertThat(userSecurityDetails.email()).isEqualTo(expectedEmail));
                }));
    }

    @ParameterizedTest
    @MethodSource("invalidJwt")
    void testUpdateAdapterShouldReturnUnauthorized(String jwtToken) throws Exception {
        // Given & When
        final var result = performPut("/api/v1/adapters/{adapterName}", jwtToken, MINIMAL_ADAPTER_JSON, "testAdapter");

        // Then
        result.andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @ParameterizedTest
    @MethodSource({"notAllowedRoles", "readOnlyAdminRoles", "reservedPrincipal"})
    void testUpdateAdapterShouldReturnForbidden(String jwtToken) throws Exception {
        // Given & When
        final var result = performPut("/api/v1/adapters/{adapterName}", jwtToken, MINIMAL_ADAPTER_JSON, "testAdapter");

        // Then
        result.andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @ParameterizedTest
    @MethodSource("fullAdminRoles")
    void testUpdateAdapterShouldReturnNoContent(String jwtToken,
                                                String expectedName,
                                                String expectedEmail,
                                                List<GrantedAuthority> expectedGrantedAuthorities) throws Exception {
        // Given & When
        final var result = performPut("/api/v1/adapters/{adapterName}", jwtToken, MINIMAL_ADAPTER_JSON, "testAdapter");

        // Then
        result
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()))
                .andExpect(authenticated().withAuthentication(auth -> {
                    assertThat(auth).isNotNull();
                    assertThat(auth.getName()).isEqualTo(expectedName);
                    assertThat(auth.getAuthorities()).isEqualTo(expectedGrantedAuthorities);
                    assertThat(auth.getDetails()).isInstanceOfSatisfying(
                            UserSecurityDetails.class,
                            userSecurityDetails -> assertThat(userSecurityDetails.email()).isEqualTo(expectedEmail));
                }));
    }

    @ParameterizedTest
    @MethodSource("invalidJwt")
    void testDeleteAdapterShouldReturnUnauthorized(String jwtToken) throws Exception {
        // Given & When
        final var result = performDelete("/api/v1/adapters/{adapterName}", jwtToken, "testAdapter");

        // Then
        result.andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @ParameterizedTest
    @MethodSource({"notAllowedRoles", "readOnlyAdminRoles", "reservedPrincipal"})
    void testDeleteAdapterShouldReturnForbidden(String jwtToken) throws Exception {
        // Given & When
        final var result = performDelete("/api/v1/adapters/{adapterName}", jwtToken, "testAdapter");

        // Then
        result.andExpect(status().is(HttpStatus.FORBIDDEN.value()));
    }

    @ParameterizedTest
    @MethodSource("fullAdminRoles")
    void testDeleteAdapterShouldReturnNoContent(String jwtToken,
                                                String expectedName,
                                                String expectedEmail,
                                                List<GrantedAuthority> expectedGrantedAuthorities) throws Exception {
        // Given & When
        final var result = performDelete("/api/v1/adapters/{adapterName}", jwtToken, "testAdapter");

        // Then
        result
                .andExpect(status().is(HttpStatus.NO_CONTENT.value()))
                .andExpect(authenticated().withAuthentication(auth -> {
                    assertThat(auth).isNotNull();
                    assertThat(auth.getName()).isEqualTo(expectedName);
                    assertThat(auth.getAuthorities()).isEqualTo(expectedGrantedAuthorities);
                    assertThat(auth.getDetails()).isInstanceOfSatisfying(
                            UserSecurityDetails.class,
                            userSecurityDetails -> assertThat(userSecurityDetails.email()).isEqualTo(expectedEmail));
                }));
    }
}
