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

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoleFacade roleFacade;

    @Test
    void testGetRole() throws Exception {
        // given
        var dtoJson = ResourceUtils.readResource("/role_dto.json");
        var dto = objectMapper.readValue(dtoJson, new TypeReference<RoleDto>() {
        });

        var dtoResponseJson = ResourceUtils.readResource("/role_dto_response.json");

        when(roleFacade.getRole(eq("test_role"))).thenReturn(dto);

        // when
        mockMvc.perform(get("/api/v1/roles/{roleName}", "test_role"))
                // then
                .andExpect(status().isOk())
                .andExpect(content().json(dtoResponseJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testCreateRole() throws Exception {
        // given
        var dtoJson = ResourceUtils.readResource("/role_dto.json");

        doNothing().when(roleFacade).createRole(any());

        // when
        mockMvc.perform(post("/api/v1/roles")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                // then
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
        // given
        var dtoJson = ResourceUtils.readResource("/role_dto.json");

        doNothing().when(roleFacade).updateRole(any(), any());

        // when
        mockMvc.perform(put("/api/v1/roles/{roleName}", "test_role")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                // then
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteRole() throws Exception {
        // given
        doNothing().when(roleFacade).deleteRole(any());

        // when
        mockMvc.perform(delete("/api/v1/roles/{roleName}", "test_role"))
                // then
                .andExpect(status().isNoContent());
    }
}