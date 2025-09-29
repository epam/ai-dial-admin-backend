package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.AdapterDto;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.AdaptersController;
import com.epam.aidial.cfg.web.facade.AdapterFacade;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AdaptersController.class)
@Import({
        JsonMapperConfiguration.class,
})
class AdaptersControllerTest extends AbstractControllerNoneSecureTest {
    private static final String DTO_JSON_PATH = "/adapter_dto.json";
    private static final String DTOS_JSON_PATH = "/adapter_dtos.json";
    private static final String TEST_ADAPTER_NAME = "test_adapter";
    private static final String ADAPTER_BASE_API_PATH = "/api/v1/adapters";
    private static final String ADAPTER_API_PATH = ADAPTER_BASE_API_PATH + "/{adapterName}";

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AdapterFacade adapterFacade;

    @Test
    void testGetAllAdapters() throws Exception {
        var dtosJson = ResourceUtils.readResource(DTOS_JSON_PATH);
        var dtos = objectMapper.readValue(dtosJson, new TypeReference<List<AdapterDto>>() {
        });

        when(adapterFacade.getAllAdapters()).thenReturn(dtos);

        mockMvc.perform(get(ADAPTER_BASE_API_PATH))
                .andExpect(status().isOk())
                .andExpect(content().json(dtosJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testGetAdapterWithoutHeaderIfNoneMatch() throws Exception {
        mockMvc.perform(get(ADAPTER_API_PATH, TEST_ADAPTER_NAME))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'If-None-Match' for method parameter type String is not present"));
    }

    @Test
    void testGetAdapterWithSameHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, AdapterDto.class);

        when(adapterFacade.getAdapterWithHash(eq(TEST_ADAPTER_NAME))).thenReturn(
                new DtoWithDomainHash<>(dto, "1"));

        mockMvc.perform(get(ADAPTER_API_PATH, TEST_ADAPTER_NAME)
                        .header(HEADER_IF_NONE_MATCH, "1"))
                .andExpect(status().isNotModified())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"1\""));
    }

    @Test
    void testGetAdapterWithDifferentHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, AdapterDto.class);

        when(adapterFacade.getAdapterWithHash(eq(TEST_ADAPTER_NAME))).thenReturn(
                new DtoWithDomainHash<>(dto, "2"));

        mockMvc.perform(get(ADAPTER_API_PATH, TEST_ADAPTER_NAME)
                        .header(HEADER_IF_NONE_MATCH, "1"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"2\""))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testUpdateAdapter() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, AdapterDto.class);

        when(adapterFacade.updateAdapter(eq(TEST_ADAPTER_NAME), any(), eq("1")))
                .thenReturn("2");

        mockMvc.perform(put(ADAPTER_API_PATH, TEST_ADAPTER_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HEADER_IF_MATCH, "1")
                        .content(dtoJson))
                .andExpect(status().isNoContent())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"2\""));
        verify(adapterFacade).updateAdapter(eq(TEST_ADAPTER_NAME), eq(dto), eq("1"));
    }

    @Test
    void testUpdateAdapterWithNotMatchHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, AdapterDto.class);

        doThrow(new OptimisticLockConflictException("Conflict Exception"))
                .when(adapterFacade).updateAdapter(eq(TEST_ADAPTER_NAME), any(), eq("1"));

        mockMvc.perform(put(ADAPTER_API_PATH, TEST_ADAPTER_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HEADER_IF_MATCH, "1")
                        .content(dtoJson))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.message").value("Conflict Exception"));
        verify(adapterFacade).updateAdapter(eq(TEST_ADAPTER_NAME), eq(dto), eq("1"));
    }

    @Test
    void testUpdateAdapterWithoutHeaderIfMatch() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);

        mockMvc.perform(put(ADAPTER_API_PATH, TEST_ADAPTER_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'If-Match' for method parameter type String is not present"));
    }

    @Test
    void testDeleteAdapter() throws Exception {

        doNothing().when(adapterFacade).deleteAdapter(eq(TEST_ADAPTER_NAME), eq(true));

        mockMvc.perform(delete(ADAPTER_API_PATH, TEST_ADAPTER_NAME))
                .andExpect(status().isNoContent());
    }
}
