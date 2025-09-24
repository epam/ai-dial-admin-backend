package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.dto.KeyDto;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.KeyController;
import com.epam.aidial.cfg.web.facade.KeyFacade;
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

@WebMvcTest(controllers = KeyController.class)
@Import({
        JsonMapperConfiguration.class,
})
class KeyControllerTest extends AbstractControllerNoneSecureTest {
    private static final String DTO_JSON_PATH = "/key_dto.json";
    private static final String DTOS_JSON_PATH = "/key_dtos.json";
    private static final String TEST_KEY_NAME = "test_key";
    private static final String KEY_BASE_API_PATH = "/api/v1/keys";
    private static final String KEY_API_PATH = KEY_BASE_API_PATH + "/{keyName}";

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KeyFacade keyFacade;

    @Test
    void testGetAllKeys() throws Exception {

        var dtosJson = ResourceUtils.readResource(DTOS_JSON_PATH);
        var dtos = objectMapper.readValue(dtosJson, new TypeReference<List<KeyDto>>() {
        });

        when(keyFacade.getAllKeys()).thenReturn(dtos);

        mockMvc.perform(get(KEY_BASE_API_PATH))
                .andExpect(status().isOk())
                .andExpect(content().json(dtosJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testGetKeyWithoutHeaderIfNoneMatch() throws Exception {
        mockMvc.perform(get(KEY_API_PATH, TEST_KEY_NAME))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'If-None-Match' for method parameter type String is not present"));
    }

    @Test
    void testGetKeyWithSameHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, new TypeReference<KeyDto>() {});

        when(keyFacade.getKeyWithHash(eq(TEST_KEY_NAME))).thenReturn(
                new DtoWithDomainHash<>(dto, "1"));

        mockMvc.perform(get(KEY_API_PATH, TEST_KEY_NAME)
                        .header(HEADER_IF_NONE_MATCH, "1"))
                .andExpect(status().isNotModified())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"1\""));
    }

    @Test
    void testGetKeyWithDifferentHash() throws Exception {

        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, new TypeReference<KeyDto>() {
        });

        when(keyFacade.getKeyWithHash(eq(TEST_KEY_NAME)))
                .thenReturn(new DtoWithDomainHash<>(dto, "2"));

        mockMvc.perform(get(KEY_API_PATH, TEST_KEY_NAME)
                        .header(HEADER_IF_NONE_MATCH, "1"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"2\""))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testCreateKey() throws Exception {

        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, new TypeReference<KeyDto>() {
        });

        mockMvc.perform(post(KEY_BASE_API_PATH)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isNoContent());

        verify(keyFacade).createKey(eq(dto));
    }

    @Test
    void testUpdateKey() throws Exception {

        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, new TypeReference<KeyDto>() {
        });

        when(keyFacade.updateKey(eq(TEST_KEY_NAME), any(), eq("1")))
                .thenReturn("2");

        mockMvc.perform(put(KEY_API_PATH, TEST_KEY_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HEADER_IF_MATCH, "1")
                        .content(dtoJson))
                .andExpect(status().isNoContent());
        verify(keyFacade).updateKey(eq(TEST_KEY_NAME), eq(dto), eq("1"));
    }

    @Test
    void testUpdateKeyWithNotMatchHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, new TypeReference<KeyDto>() {
        });

        doThrow(new OptimisticLockConflictException("Conflict Exception"))
                .when(keyFacade).updateKey(eq(TEST_KEY_NAME), any(), eq("1"));

        mockMvc.perform(put(KEY_API_PATH, TEST_KEY_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HEADER_IF_MATCH, "1")
                        .content(dtoJson))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.message").value("Conflict Exception"));
        verify(keyFacade).updateKey(eq(TEST_KEY_NAME), eq(dto), eq("1"));
    }

    @Test
    void testUpdateKeyWithoutHeaderIfMatch() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);

        mockMvc.perform(put(KEY_API_PATH, TEST_KEY_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'If-Match' for method parameter type String is not present"));
    }

    @Test
    void testDeleteKey() throws Exception {
        mockMvc.perform(delete(KEY_API_PATH, TEST_KEY_NAME))
                .andExpect(status().isNoContent());
        verify(keyFacade).deleteKey(eq(TEST_KEY_NAME));
    }
}
