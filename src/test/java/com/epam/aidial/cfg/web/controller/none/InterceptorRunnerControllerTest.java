package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.dto.InterceptorRunnerDto;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.InterceptorRunnerController;
import com.epam.aidial.cfg.web.facade.InterceptorRunnerFacade;
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

@WebMvcTest(controllers = InterceptorRunnerController.class)
@Import({
        JsonMapperConfiguration.class,
})
class InterceptorRunnerControllerTest extends AbstractControllerNoneSecureTest {
    private static final String DTO_JSON_PATH = "/interceptor_runner_dto.json";
    private static final String DTOS_JSON_PATH = "/interceptor_runner_dtos.json";
    private static final String TEST_INTERCEPTOR_RUNNER_NAME = "test_interceptor-runner";
    private static final String INTERCEPTOR_RUNNER_BASE_API_PATH = "/api/v1/interceptor-runners";
    private static final String INTERCEPTOR_RUNNER_API_PATH = INTERCEPTOR_RUNNER_BASE_API_PATH + "/{interceptorRunnerName}";

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private InterceptorRunnerFacade interceptorRunnerFacade;

    @Test
    void testGetAllInterceptorRunners() throws Exception {
        var dtosJson = ResourceUtils.readResource(DTOS_JSON_PATH);
        var dtos = objectMapper.readValue(dtosJson, new TypeReference<List<InterceptorRunnerDto>>() {
        });

        when(interceptorRunnerFacade.getAllInterceptorRunners()).thenReturn(dtos);

        mockMvc.perform(get(INTERCEPTOR_RUNNER_BASE_API_PATH))
                .andExpect(status().isOk())
                .andExpect(content().json(dtosJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testGetInterceptorRunnerWithoutHeaderIfNoneMatch() throws Exception {
        mockMvc.perform(get(INTERCEPTOR_RUNNER_API_PATH, TEST_INTERCEPTOR_RUNNER_NAME))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'If-None-Match' for method parameter type String is not present"));
    }

    @Test
    void testGetInterceptorRunnerWithSameHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, InterceptorRunnerDto.class);

        when(interceptorRunnerFacade.getInterceptorRunnerWithHash(eq(TEST_INTERCEPTOR_RUNNER_NAME)))
                .thenReturn(new DtoWithDomainHash<>(dto, "1"));

        mockMvc.perform(get(INTERCEPTOR_RUNNER_API_PATH, TEST_INTERCEPTOR_RUNNER_NAME)
                        .header(HEADER_IF_NONE_MATCH, "1"))
                .andExpect(status().isNotModified())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"1\""));
    }

    @Test
    void testGetInterceptorRunnerWithDifferentHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, InterceptorRunnerDto.class);

        when(interceptorRunnerFacade.getInterceptorRunnerWithHash(eq(TEST_INTERCEPTOR_RUNNER_NAME)))
                .thenReturn(new DtoWithDomainHash<>(dto, "2"));

        mockMvc.perform(get(INTERCEPTOR_RUNNER_API_PATH, TEST_INTERCEPTOR_RUNNER_NAME)
                        .header(HEADER_IF_NONE_MATCH, "1"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"2\""))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testUpdateInterceptorRunner() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, InterceptorRunnerDto.class);

        when(interceptorRunnerFacade.updateInterceptorRunner(eq(TEST_INTERCEPTOR_RUNNER_NAME), any(), eq("1")))
                .thenReturn("2");

        mockMvc.perform(put(INTERCEPTOR_RUNNER_API_PATH, TEST_INTERCEPTOR_RUNNER_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HEADER_IF_MATCH, "1")
                        .content(dtoJson))
                .andExpect(status().isNoContent())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"2\""));
        verify(interceptorRunnerFacade).updateInterceptorRunner(eq(TEST_INTERCEPTOR_RUNNER_NAME), eq(dto), eq("1"));
    }

    @Test
    void testUpdateInterceptorRunnerWithNotMatchHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, InterceptorRunnerDto.class);

        doThrow(new OptimisticLockConflictException("Conflict Exception"))
                .when(interceptorRunnerFacade).updateInterceptorRunner(eq(TEST_INTERCEPTOR_RUNNER_NAME), any(), eq("1"));

        mockMvc.perform(put(INTERCEPTOR_RUNNER_API_PATH, TEST_INTERCEPTOR_RUNNER_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HEADER_IF_MATCH, "1")
                        .content(dtoJson))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.message").value("Conflict Exception"));
        verify(interceptorRunnerFacade).updateInterceptorRunner(eq(TEST_INTERCEPTOR_RUNNER_NAME), eq(dto), eq("1"));
    }

    @Test
    void testUpdateInterceptorRunnerWithoutHeaderIfMatch() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);

        mockMvc.perform(put(INTERCEPTOR_RUNNER_API_PATH, TEST_INTERCEPTOR_RUNNER_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'If-Match' for method parameter type String is not present"));
    }

    @Test
    void testDeleteInterceptorRunner() throws Exception {

        doNothing().when(interceptorRunnerFacade).deleteInterceptorRunner(eq(TEST_INTERCEPTOR_RUNNER_NAME), eq(true));

        mockMvc.perform(delete(INTERCEPTOR_RUNNER_API_PATH, TEST_INTERCEPTOR_RUNNER_NAME))
                .andExpect(status().isNoContent());
    }
}
