package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.AddonDto;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.AddonController;
import com.epam.aidial.cfg.web.facade.AddonFacade;
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

@WebMvcTest(controllers = AddonController.class)
@Import({
        JsonMapperConfiguration.class,
})
public class AddonControllerTest extends AbstractControllerNoneSecureTest {
    private static final String DTO_JSON_PATH = "/addon_dto.json";
    private static final String DTOS_JSON_PATH = "/addon_dtos.json";
    private static final String TEST_ADDON_NAME = "test_addon";
    private static final String ADDON_BASE_API_PATH = "/api/v1/addons";
    private static final String ADDON_API_PATH = ADDON_BASE_API_PATH + "/{addonName}";

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AddonFacade addonFacade;

    @Test
    void testGetAllAddons() throws Exception {
        var dtosJson = ResourceUtils.readResource(DTOS_JSON_PATH);
        var dtos = objectMapper.readValue(dtosJson, new TypeReference<List<AddonDto>>() {
        });

        when(addonFacade.getAllAddons()).thenReturn(dtos);

        mockMvc.perform(get(ADDON_BASE_API_PATH))
                .andExpect(status().isOk())
                .andExpect(content().json(dtosJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testGetAddonWithoutHeaderIfNoneMatch() throws Exception {
        mockMvc.perform(get(ADDON_API_PATH, TEST_ADDON_NAME))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'If-None-Match' for method parameter type String is not present"));
    }

    @Test
    void testGetAddonWithSameHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, AddonDto.class);

        when(addonFacade.getAddonWithHash(eq(TEST_ADDON_NAME))).thenReturn(
                new DtoWithDomainHash<>(dto, "1"));

        mockMvc.perform(get(ADDON_API_PATH, TEST_ADDON_NAME)
                        .header(HEADER_IF_NONE_MATCH, "1"))
                .andExpect(status().isNotModified())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"1\""));
    }

    @Test
    void testGetAddonWithDifferentHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, AddonDto.class);

        when(addonFacade.getAddonWithHash(eq(TEST_ADDON_NAME))).thenReturn(
                new DtoWithDomainHash<>(dto, "2"));

        mockMvc.perform(get(ADDON_API_PATH, TEST_ADDON_NAME)
                        .header(HEADER_IF_NONE_MATCH, "1"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"2\""))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testUpdateAddon() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, AddonDto.class);

        when(addonFacade.updateAddon(eq(TEST_ADDON_NAME), any(), eq("1")))
                .thenReturn("2");

        mockMvc.perform(put(ADDON_API_PATH, TEST_ADDON_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HEADER_IF_MATCH, "1")
                        .content(dtoJson))
                .andExpect(status().isNoContent())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"2\""));
        verify(addonFacade).updateAddon(eq(TEST_ADDON_NAME), eq(dto), eq("1"));
    }

    @Test
    void testUpdateAddonWithNotMatchHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, AddonDto.class);

        doThrow(new OptimisticLockConflictException("Conflict Exception"))
                .when(addonFacade).updateAddon(eq(TEST_ADDON_NAME), any(), eq("1"));

        mockMvc.perform(put(ADDON_API_PATH, TEST_ADDON_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HEADER_IF_MATCH, "1")
                        .content(dtoJson))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.message").value("Conflict Exception"));
        verify(addonFacade).updateAddon(eq(TEST_ADDON_NAME), eq(dto), eq("1"));
    }

    @Test
    void testUpdateAddonWithoutHeaderIfMatch() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);

        mockMvc.perform(put(ADDON_API_PATH, TEST_ADDON_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'If-Match' for method parameter type String is not present"));
    }

    @Test
    void testDeleteAddon() throws Exception {
        doNothing().when(addonFacade).deleteAddon(eq(TEST_ADDON_NAME));
        mockMvc.perform(delete(ADDON_API_PATH, TEST_ADDON_NAME))
                .andExpect(status().isNoContent());
    }
}
