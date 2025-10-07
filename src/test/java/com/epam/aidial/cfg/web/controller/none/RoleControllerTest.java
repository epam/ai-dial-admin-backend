package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.RoleController;
import com.epam.aidial.cfg.web.facade.RoleFacade;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RoleController.class)
@Import({
        JsonMapperConfiguration.class,
})
class RoleControllerTest extends AbstractControllerNoneSecureTest {
    private static final String DTO_JSON_PATH = "/role_dto.json";
    private static final String DTO_RESPONSE_JSON_PATH = "/role_dto_response.json";
    private static final String TEST_ROLE_NAME = "testRole";
    private static final String ROLE_BASE_API_PATH = "/api/v1/roles";
    private static final String ROLE_API_PATH = ROLE_BASE_API_PATH + "/{roleName}";

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoleFacade roleFacade;

    @Test
    void testGetRole() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, new TypeReference<RoleDto>() {
        });

        var dtoResponseJson = ResourceUtils.readResource(DTO_RESPONSE_JSON_PATH);

        when(roleFacade.getRole(TEST_ROLE_NAME)).thenReturn(dto);

        mockMvc.perform(get(ROLE_API_PATH, TEST_ROLE_NAME))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json(dtoResponseJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testCreateRole() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);

        doNothing().when(roleFacade).createRole(any());
        mockMvc.perform(post(ROLE_BASE_API_PATH)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isNoContent());
    }

    @Test
    void testCreateRole_WithInvalidResourceTypeInShare_BadRequest() throws Exception {
        // given
        var dtoJson = ResourceUtils.readResource("/role_dto_with_invalid_resource_type_in_share.json");

        // when
        mockMvc.perform(post("/api/v1/roles")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("JSON parse error: Cannot deserialize "
                        + "Map key of type `com.epam.aidial.cfg.dto.ResourceTypeDto` from String \"invalid\": "
                        + "not a valid representation, problem: (java.lang.IllegalArgumentException) Invalid resource type: invalid"));
    }

    @Test
    void testCreateRole_WithEmptyResourceTypeInShare_BadRequest() throws Exception {
        // given
        var dtoJson = ResourceUtils.readResource("/role_dto_with_empty_resource_type_in_share.json");

        // when
        mockMvc.perform(post("/api/v1/roles")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("JSON parse error: Cannot deserialize "
                        + "Map key of type `com.epam.aidial.cfg.dto.ResourceTypeDto` from String \"\": "
                        + "not a valid representation, problem: (java.lang.IllegalArgumentException) Invalid resource type: "));
    }

    @Test
    void testUpdateRole() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);

        doNothing().when(roleFacade).updateRole(eq(TEST_ROLE_NAME), any());

        mockMvc.perform(put(ROLE_API_PATH, TEST_ROLE_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteRole() throws Exception {
        doNothing().when(roleFacade).deleteRole(any());
        mockMvc.perform(delete(ROLE_API_PATH, TEST_ROLE_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());
    }
}