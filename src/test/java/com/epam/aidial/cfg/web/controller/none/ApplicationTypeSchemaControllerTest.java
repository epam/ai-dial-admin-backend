package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.ApplicationTypeSchemaDto;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.ApplicationTypeSchemaController;
import com.epam.aidial.cfg.web.facade.ApplicationTypeSchemaFacade;
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

@WebMvcTest(controllers = ApplicationTypeSchemaController.class)
@Import({
        JsonMapperConfiguration.class,
})
class ApplicationTypeSchemaControllerTest extends AbstractControllerNoneSecureTest {

    private static final String DTO_JSON_PATH = "/application_type_schema_dto.json";
    private static final String DTOS_JSON_PATH = "/application_type_schema_dtos.json";
    private static final String TEST_SCHEMA_NAME = "test-schema";
    private static final String ID = "id";
    private static final String SCHEMA_BASE_API_PATH = "/api/v1/applicationTypeSchemas";

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApplicationTypeSchemaFacade schemaFacade;

    @Test
    void testGetAll() throws Exception {
        // given
        var dtosJson = ResourceUtils.readResource(DTOS_JSON_PATH);
        var dtos = objectMapper.readValue(dtosJson, new TypeReference<List<ApplicationTypeSchemaDto>>() {
        });

        when(schemaFacade.getAll()).thenReturn(dtos);
        // when
        mockMvc.perform(get(SCHEMA_BASE_API_PATH))
                //then
                .andExpect(status().isOk())
                .andExpect(content().json(dtosJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testGetSchemaWithSameHash() throws Exception {
        // given
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, new TypeReference<ApplicationTypeSchemaDto>() {
        });

        when(schemaFacade.getSchemaWithHash(eq(TEST_SCHEMA_NAME))).thenReturn(new DtoWithDomainHash<>(dto, "1"));
        // when
        mockMvc.perform(get(SCHEMA_BASE_API_PATH)
                        .param(ID, TEST_SCHEMA_NAME)
                        .header(HEADER_IF_NONE_MATCH, "1"))
                .andExpect(status().isNotModified())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"1\""));
    }

    @Test
    void testGetSchemaWithoutHeaderIfNoneMatch() throws Exception {
        mockMvc.perform(get(SCHEMA_BASE_API_PATH)
                        .param(ID, TEST_SCHEMA_NAME))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Header 'If-None-Match' is required when 'id' parameter is provided"));
    }

    @Test
    void testGetSchemaByIdWhenSchemaNotExist() throws Exception {
        doThrow(new EntityNotFoundException("Not found"))
                .when(schemaFacade).getSchemaWithHash(eq(TEST_SCHEMA_NAME));

        mockMvc.perform(get(SCHEMA_BASE_API_PATH, TEST_SCHEMA_NAME)
                        .param(ID, TEST_SCHEMA_NAME)
                        .header(HEADER_IF_NONE_MATCH, "1"))
                .andExpect(jsonPath("$.message")
                        .value("Not found"));
    }

    @Test
    void testGetSchemaWithDifferentHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, new TypeReference<ApplicationTypeSchemaDto>() {
        });
        when(schemaFacade.getSchemaWithHash(eq(TEST_SCHEMA_NAME))).thenReturn(new DtoWithDomainHash<>(dto, "2"));

        mockMvc.perform(get(SCHEMA_BASE_API_PATH, TEST_SCHEMA_NAME)
                        .param(ID, TEST_SCHEMA_NAME)
                        .header(HEADER_IF_NONE_MATCH, "1"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"2\""))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testCreate() throws Exception {
        // given
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);

        doNothing().when(schemaFacade).create(any());
        // when
        mockMvc.perform(post(SCHEMA_BASE_API_PATH)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                // then
                .andExpect(status().isNoContent());
    }

    @Test
    void testUpdate() throws Exception {
        // given
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);

        when(schemaFacade.update(any(), any(ApplicationTypeSchemaDto.class), any())).thenReturn("2");
        // when
        mockMvc.perform(put(SCHEMA_BASE_API_PATH)
                        .param(ID, TEST_SCHEMA_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HEADER_IF_MATCH, "1")
                        .content(dtoJson))
                // then
                .andExpect(status().isNoContent());
    }

    @Test
    void testUpdateSchemaWithNotMatchHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, new TypeReference<ApplicationTypeSchemaDto>() {
        });

        doThrow(new OptimisticLockConflictException("Conflict Exception"))
                .when(schemaFacade).update(eq(TEST_SCHEMA_NAME), any(ApplicationTypeSchemaDto.class), eq("1"));

        mockMvc.perform(put(SCHEMA_BASE_API_PATH, TEST_SCHEMA_NAME)
                        .param(ID, TEST_SCHEMA_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HEADER_IF_MATCH, "1")
                        .content(dtoJson))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.message").value("Conflict Exception"));
        verify(schemaFacade).update(eq(TEST_SCHEMA_NAME), eq(dto), eq("1"));
    }

    @Test
    void testUpdateSchemaWithoutHeaderIfMatch() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);

        mockMvc.perform(put(SCHEMA_BASE_API_PATH, TEST_SCHEMA_NAME)
                        .param(ID, TEST_SCHEMA_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'If-Match' for method parameter type String is not present"));
    }

    @Test
    void testDelete() throws Exception {
        // given
        doNothing().when(schemaFacade).delete(any(), eq(false));
        // when
        mockMvc.perform(delete(SCHEMA_BASE_API_PATH)
                        .param(ID, TEST_SCHEMA_NAME))
                // then
                .andExpect(status().isNoContent());
    }
}
