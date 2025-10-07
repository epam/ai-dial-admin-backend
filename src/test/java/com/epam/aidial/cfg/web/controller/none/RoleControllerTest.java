package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.dto.RoleDto;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
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

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RoleController.class)
@Import({
        JsonMapperConfiguration.class,
})
class RoleControllerTest extends AbstractControllerNoneSecureTest {
    private static final String DTO_JSON_PATH = "/role_dto.json";
    private static final String TEST_ROLE_NAME = "testRole";
    private static final String ROLE_BASE_API_PATH = "/api/v1/roles";
    private static final String ROLE_API_PATH = ROLE_BASE_API_PATH + "/{roleName}";

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RoleFacade roleFacade;

    @Test
    void testGetAllRoles() throws Exception {
        var dtosJson = ResourceUtils.readResource("/role_dtos.json");
        var dtos = objectMapper.readValue(dtosJson, new TypeReference<List<RoleDto>>() {
        });

        when(roleFacade.getAllRoles()).thenReturn(dtos);

        mockMvc.perform(get(ROLE_BASE_API_PATH))
                .andExpect(status().isOk())
                .andExpect(content().json(dtosJson, JsonCompareMode.LENIENT));

    }

    @Test
    void testGetRoleWithoutHeaderIfNoneMatch() throws Exception {
        mockMvc.perform(get(ROLE_API_PATH, TEST_ROLE_NAME))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'If-None-Match' for method parameter type String is not present"));
    }

    @Test
    void testGetRoleWithSameHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, new TypeReference<RoleDto>() {
        });

        when(roleFacade.getRoleWithHash(eq(TEST_ROLE_NAME))).thenReturn(
                new DtoWithDomainHash<>(dto, "1"));

        mockMvc.perform(get(ROLE_API_PATH, TEST_ROLE_NAME)
                        .header(HEADER_IF_NONE_MATCH, "1"))
                .andExpect(status().isNotModified())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"1\""));
    }

    @Test
    void testGetRoleWithDifferentHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, new TypeReference<RoleDto>() {
        });

        when(roleFacade.getRoleWithHash(eq(TEST_ROLE_NAME))).thenReturn(
                new DtoWithDomainHash<>(dto, "2"));

        mockMvc.perform(get(ROLE_API_PATH, TEST_ROLE_NAME)
                        .header(HEADER_IF_NONE_MATCH, "1"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"2\""))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
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

        when(roleFacade.updateRole(eq(TEST_ROLE_NAME), any(), eq("1")))
                .thenReturn("2");

        mockMvc.perform(put(ROLE_API_PATH, TEST_ROLE_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HEADER_IF_MATCH, "1")
                        .content(dtoJson))
                .andExpect(status().isNoContent())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"2\""));
    }

    @Test
    void testUpdateRoleWithNotMatchHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, new TypeReference<RoleDto>() {
        });

        doThrow(new OptimisticLockConflictException("Conflict Exception"))
                .when(roleFacade).updateRole(eq(TEST_ROLE_NAME), any(), eq("1"));

        mockMvc.perform(put(ROLE_API_PATH, TEST_ROLE_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HEADER_IF_MATCH, "1")
                        .content(dtoJson))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.message").value("Conflict Exception"));
        verify(roleFacade).updateRole(eq(TEST_ROLE_NAME), eq(dto), eq("1"));
    }

    @Test
    void testUpdateRoleWithoutHeaderIfMatch() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);

        mockMvc.perform(put(ROLE_API_PATH, TEST_ROLE_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'If-Match' for method parameter type String is not present"));
    }

    @Test
    void testDeleteRole() throws Exception {
        doNothing().when(roleFacade).deleteRole(any());
        mockMvc.perform(delete(ROLE_API_PATH, TEST_ROLE_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(status().isNoContent());
    }
}