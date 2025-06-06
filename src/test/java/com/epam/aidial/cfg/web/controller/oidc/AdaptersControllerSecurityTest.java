package com.epam.aidial.cfg.web.controller.oidc;

import com.epam.aidial.cfg.service.AdapterService;
import com.epam.aidial.cfg.web.controller.AdaptersController;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdaptersController.class)
class AdaptersControllerSecurityTest extends AbstractControllerSecurityTest {

    @MockitoBean
    private AdapterService adapterService;

    @ParameterizedTest
    @MethodSource("arguments")
    void testGetAllKeys(final String jwtToken,
                        final HttpStatus expectedStatus) throws Exception {
        // Given & When
        final var result = performGet("/api/v1/adapters", jwtToken);

        // Then
        result.andExpect(status().is(expectedStatus.value()));
    }
}
