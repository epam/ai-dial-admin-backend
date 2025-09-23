package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.dto.InterceptorDto;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.InterceptorController;
import com.epam.aidial.cfg.web.facade.InterceptorFacade;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = InterceptorController.class)
@Import({
        JsonMapperConfiguration.class,
})
public class InterceptorControllerTest extends AbstractControllerNoneSecureTest {
    private static final String DTO_JSON_PATH = "/interceptor_dto.json";
    private static final String DTOS_JSON_PATH = "/interceptor_dtos.json";
    private static final String TEST_INTERCEPTOR_NAME = "test_interceptor";
    private static final String INTERCEPTOR_BASE_API_PATH = "/api/v1/interceptors";
    private static final String INTERCEPTOR_API_PATH = INTERCEPTOR_BASE_API_PATH + "/{interceptorName}";

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InterceptorFacade interceptorFacade;

    @Test
    void testGetAllInterceptors() throws Exception {
        var dtosJson = ResourceUtils.readResource(DTOS_JSON_PATH);
        var dtos = objectMapper.readValue(dtosJson, new TypeReference<List<InterceptorDto>>() {
        });

        when(interceptorFacade.getAllInterceptors()).thenReturn(dtos);

        mockMvc.perform(get(INTERCEPTOR_BASE_API_PATH))
                .andExpect(status().isOk())
                .andExpect(content().json(dtosJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testGetInterceptorWithoutHeaderIfNoneMatch() throws Exception {
        mockMvc.perform(get(INTERCEPTOR_API_PATH, TEST_INTERCEPTOR_NAME))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'If-None-Match' for method parameter type String is not present"));
    }

    @Test
    void testGetInterceptorWithSameHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, InterceptorDto.class);

        when(interceptorFacade.getInterceptorWithHash(eq(TEST_INTERCEPTOR_NAME))).thenReturn(
                new DtoWithDomainHash<>(dto, "1"));

        mockMvc.perform(get(INTERCEPTOR_API_PATH, TEST_INTERCEPTOR_NAME)
                        .header(HEADER_IF_NONE_MATCH, "1"))
                .andExpect(status().isNotModified())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"1\""));
    }

    @Test
    void testGetInterceptorWithDifferentHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, InterceptorDto.class);

        when(interceptorFacade.getInterceptorWithHash(eq(TEST_INTERCEPTOR_NAME))).thenReturn(
                new DtoWithDomainHash<>(dto, "2"));

        mockMvc.perform(get(INTERCEPTOR_API_PATH, TEST_INTERCEPTOR_NAME)
                        .header(HEADER_IF_NONE_MATCH, "1"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"2\""))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testUpdateInterceptor() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, InterceptorDto.class);

        when(interceptorFacade.updateInterceptor(eq(TEST_INTERCEPTOR_NAME), any(), eq("1")))
                .thenReturn("2");

        mockMvc.perform(put(INTERCEPTOR_API_PATH, TEST_INTERCEPTOR_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HEADER_IF_MATCH, "1")
                        .content(dtoJson))
                .andExpect(status().isNoContent())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"2\""));
        verify(interceptorFacade).updateInterceptor(eq(TEST_INTERCEPTOR_NAME), eq(dto), eq("1"));
    }

    @Test
    void testUpdateInterceptorWithNotMatchHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, InterceptorDto.class);

        doThrow(new OptimisticLockConflictException("Conflict Exception"))
                .when(interceptorFacade).updateInterceptor(eq(TEST_INTERCEPTOR_NAME), any(), eq("1"));

        mockMvc.perform(put(INTERCEPTOR_API_PATH, TEST_INTERCEPTOR_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HEADER_IF_MATCH, "1")
                        .content(dtoJson))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.message").value("Conflict Exception"));
        verify(interceptorFacade).updateInterceptor(eq(TEST_INTERCEPTOR_NAME), eq(dto), eq("1"));
    }

    @Test
    void testUpdateInterceptorWithoutHeaderIfMatch() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);

        mockMvc.perform(put(INTERCEPTOR_API_PATH, TEST_INTERCEPTOR_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'If-Match' for method parameter type String is not present"));
    }
}


