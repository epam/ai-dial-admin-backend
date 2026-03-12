package com.epam.aidial.cfg.web.controller.oidc;

import com.epam.aidial.cfg.utils.JwtUtils;
import com.epam.aidial.cfg.web.controller.AdaptersController;
import com.epam.aidial.cfg.web.controller.ModelController;
import com.epam.aidial.cfg.web.facade.AdapterFacade;
import com.epam.aidial.cfg.web.facade.ModelFacade;
import com.epam.aidial.cfg.web.security.AdminRole;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Verifies that READ_ONLY_ADMIN users can access read endpoints but are blocked from mutating endpoints.
 * Uses AdaptersController (read) and ModelController (create/delete) as representative controllers.
 */
@WebMvcTest(controllers = {AdaptersController.class, ModelController.class})
class ReadOnlyAdminSecurityTest extends AbstractReadOnlyAdminSecurityTest {

    @MockitoBean
    private AdapterFacade adapterFacade;

    @MockitoBean
    private ModelFacade modelFacade;

    private String fullAdminToken() {
        return JwtUtils.generateTestToken(TEST_AUDIENCE, TEST_ISSUER,
                Map.of(PRINCIPAL_CLAIM, "full_admin_user", ROLES_CLAIM, "FullAdminRole"));
    }

    private String readOnlyToken() {
        return JwtUtils.generateTestToken(TEST_AUDIENCE, TEST_ISSUER,
                Map.of(PRINCIPAL_CLAIM, "readonly_user", ROLES_CLAIM, "ReadOnlyRole"));
    }

    // ---- READ endpoint tests ----

    @Test
    void readOnlyAdmin_canAccessGetEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/adapters")
                        .header("Authorization", "Bearer " + readOnlyToken()))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(authenticated().withAuthentication(auth -> {
                    assertThat(auth.getAuthorities())
                            .extracting("authority")
                            .containsExactly(AdminRole.READ_ONLY_ADMIN.name());
                }));
    }

    @Test
    void fullAdmin_canAccessGetEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/adapters")
                        .header("Authorization", "Bearer " + fullAdminToken()))
                .andExpect(status().is(HttpStatus.OK.value()))
                .andExpect(authenticated().withAuthentication(auth -> {
                    assertThat(auth.getAuthorities())
                            .extracting("authority")
                            .containsExactly(AdminRole.FULL_ADMIN.name());
                }));
    }

    // ---- MUTATING endpoint tests ----
    // Note: AccessDeniedException is mapped to 401 (Unauthorized) by DefaultExceptionHandler

    private static final String VALID_MODEL_JSON = "{\"name\":\"testModel\",\"displayName\":\"Test Model\"}";

    @Test
    void readOnlyAdmin_cannotCreateModel() throws Exception {
        mockMvc.perform(post("/api/v1/models")
                        .header("Authorization", "Bearer " + readOnlyToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_MODEL_JSON))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    void readOnlyAdmin_cannotDeleteModel() throws Exception {
        mockMvc.perform(delete("/api/v1/models/someModel")
                        .header("Authorization", "Bearer " + readOnlyToken()))
                .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
    }

    @Test
    void fullAdmin_canCreateModel() throws Exception {
        mockMvc.perform(post("/api/v1/models")
                        .header("Authorization", "Bearer " + fullAdminToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(VALID_MODEL_JSON))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    void fullAdmin_canDeleteModel() throws Exception {
        mockMvc.perform(delete("/api/v1/models/someModel")
                        .header("Authorization", "Bearer " + fullAdminToken()))
                .andExpect(status().is2xxSuccessful());
    }
}
