package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.CatalogSchemaDto;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.web.controller.CatalogSchemaController;
import com.epam.aidial.cfg.web.facade.CatalogSchemaFacade;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CatalogSchemaController.class)
@Import({JsonMapperConfiguration.class})
class CatalogSchemaControllerTest extends AbstractControllerNoneSecureTest {

    private static final String TEST_SCHEMA_ID = "test-schema-id";
    private static final String SCHEMA_BASE_API_PATH = "/api/v1/catalogSchemas";

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CatalogSchemaFacade catalogSchemaFacade;

    @Test
    void testGetAll() throws Exception {
        CatalogSchemaDto dto = new CatalogSchemaDto();
        dto.setId(TEST_SCHEMA_ID);
        dto.setCatalogEntityType(CatalogSchemaDto.CatalogEntityTypeDto.MODEL);
        dto.setCatalogDisplayName("Model Catalog");
        dto.setDefaultLocale("en");

        when(catalogSchemaFacade.getAll()).thenReturn(List.of(dto));

        mockMvc.perform(get(SCHEMA_BASE_API_PATH))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].$id").value(TEST_SCHEMA_ID));
    }

    @Test
    void testGetSchemaWithSameHash() throws Exception {
        CatalogSchemaDto dto = new CatalogSchemaDto();
        dto.setId(TEST_SCHEMA_ID);
        dto.setCatalogEntityType(CatalogSchemaDto.CatalogEntityTypeDto.MODEL);
        dto.setCatalogDisplayName("Model Catalog");
        dto.setDefaultLocale("en");

        when(catalogSchemaFacade.getSchemaWithHash(eq(TEST_SCHEMA_ID)))
                .thenReturn(new DtoWithDomainHash<>(dto, "hash123"));

        mockMvc.perform(get(SCHEMA_BASE_API_PATH)
                        .param("id", TEST_SCHEMA_ID)
                        .header("If-None-Match", "hash123"))
                .andExpect(status().isNotModified())
                .andExpect(header().exists("ETag"))
                .andExpect(header().string("ETag", "\"hash123\""));
    }

    @Test
    void testGetSchemaWithoutHeaderIfNoneMatch() throws Exception {
        mockMvc.perform(get(SCHEMA_BASE_API_PATH)
                        .param("id", TEST_SCHEMA_ID))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Header 'If-None-Match' is required when 'id' parameter is provided"));
    }

    @Test
    void testGetSchemaByIdWhenSchemaNotExist() throws Exception {
        doThrow(new EntityNotFoundException("Not found"))
                .when(catalogSchemaFacade).getSchemaWithHash(eq(TEST_SCHEMA_ID));

        mockMvc.perform(get(SCHEMA_BASE_API_PATH)
                        .param("id", TEST_SCHEMA_ID)
                        .header("If-None-Match", "hash123"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testCreateCatalogSchema() throws Exception {
        CatalogSchemaDto dto = new CatalogSchemaDto();
        dto.setId(TEST_SCHEMA_ID);
        dto.setCatalogEntityType(CatalogSchemaDto.CatalogEntityTypeDto.MODEL);
        dto.setCatalogDisplayName("Model Catalog");
        dto.setDefaultLocale("en");

        doNothing().when(catalogSchemaFacade).create(any(CatalogSchemaDto.class));

        mockMvc.perform(post(SCHEMA_BASE_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent());

        verify(catalogSchemaFacade).create(any(CatalogSchemaDto.class));
    }

    @Test
    void testUpdateCatalogSchema() throws Exception {
        CatalogSchemaDto dto = new CatalogSchemaDto();
        dto.setId(TEST_SCHEMA_ID);
        dto.setCatalogEntityType(CatalogSchemaDto.CatalogEntityTypeDto.MODEL);
        dto.setCatalogDisplayName("Model Catalog");
        dto.setDefaultLocale("en");

        when(catalogSchemaFacade.update(eq(TEST_SCHEMA_ID), any(CatalogSchemaDto.class), eq("oldHash")))
                .thenReturn("newHash");

        mockMvc.perform(put(SCHEMA_BASE_API_PATH)
                        .param("id", TEST_SCHEMA_ID)
                        .header("If-Match", "\"oldHash\"")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNoContent())
                .andExpect(header().string("ETag", "\"newHash\""));
    }

    @Test
    void testUpdateWithOptimisticLockConflict() throws Exception {
        CatalogSchemaDto dto = new CatalogSchemaDto();
        dto.setId(TEST_SCHEMA_ID);
        dto.setCatalogEntityType(CatalogSchemaDto.CatalogEntityTypeDto.MODEL);
        dto.setCatalogDisplayName("Model Catalog");
        dto.setDefaultLocale("en");

        doThrow(new OptimisticLockConflictException("Conflict"))
                .when(catalogSchemaFacade).update(eq(TEST_SCHEMA_ID), any(CatalogSchemaDto.class), eq("oldHash"));

        mockMvc.perform(put(SCHEMA_BASE_API_PATH)
                        .param("id", TEST_SCHEMA_ID)
                        .header("If-Match", "\"oldHash\"")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isPreconditionFailed());
    }

    @Test
    void testDeleteCatalogSchema() throws Exception {
        doNothing().when(catalogSchemaFacade).delete(eq(TEST_SCHEMA_ID));

        mockMvc.perform(delete(SCHEMA_BASE_API_PATH)
                        .param("id", TEST_SCHEMA_ID))
                .andExpect(status().isNoContent());

        verify(catalogSchemaFacade).delete(eq(TEST_SCHEMA_ID));
    }

    @Test
    void testGetSnapshot() throws Exception {
        CatalogSchemaDto dto = new CatalogSchemaDto();
        dto.setId(TEST_SCHEMA_ID);
        dto.setCatalogEntityType(CatalogSchemaDto.CatalogEntityTypeDto.MODEL);
        dto.setCatalogDisplayName("Model Catalog");
        dto.setDefaultLocale("en");

        when(catalogSchemaFacade.getSnapshot(eq(TEST_SCHEMA_ID), eq(1)))
                .thenReturn(dto);

        mockMvc.perform(get(SCHEMA_BASE_API_PATH + "/snapshot")
                        .param("id", TEST_SCHEMA_ID)
                        .param("revision", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.$id").value(TEST_SCHEMA_ID));
    }

    @Test
    void testGetAllAtRevision() throws Exception {
        CatalogSchemaDto dto = new CatalogSchemaDto();
        dto.setId(TEST_SCHEMA_ID);
        dto.setCatalogEntityType(CatalogSchemaDto.CatalogEntityTypeDto.MODEL);
        dto.setCatalogDisplayName("Model Catalog");
        dto.setDefaultLocale("en");

        when(catalogSchemaFacade.getAllAtRevision(eq(1)))
                .thenReturn(List.of(dto));

        mockMvc.perform(get(SCHEMA_BASE_API_PATH + "/revision/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].$id").value(TEST_SCHEMA_ID));
    }
}
