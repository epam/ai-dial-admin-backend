package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ApplicationInfoDto;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.ApplicationController;
import com.epam.aidial.cfg.web.facade.ApplicationFacade;
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

import static org.hamcrest.CoreMatchers.containsString;
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

@WebMvcTest(controllers = ApplicationController.class)
@Import({
        JsonMapperConfiguration.class,
})
class ApplicationControllerTest extends AbstractControllerNoneSecureTest {
    private static final String DTO_APPLICATION_PATH = "/application_dto.json";
    private static final String DTOS_JSON_PATH = "/application_info_dtos.json";
    private static final String TEST_APPLICATION_NAME = "test_application";
    private static final String APPLICATION_BASE_API_PATH = "/api/v1/applications";
    private static final String APPLICATION_API_PATH = APPLICATION_BASE_API_PATH + "/{applicationName}";

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApplicationFacade applicationFacade;

    @Test
    void testGetAllApplications() throws Exception {
        var dtosJson = ResourceUtils.readResource(DTOS_JSON_PATH);
        var dtos = objectMapper.readValue(dtosJson, new TypeReference<List<ApplicationInfoDto>>() {
        });

        when(applicationFacade.getAllApplications()).thenReturn(dtos);

        mockMvc.perform(get(APPLICATION_BASE_API_PATH))
                .andExpect(status().isOk())
                .andExpect(content().json(dtosJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testGetApplicationWithoutHeaderIfNoneMatch() throws Exception {
        mockMvc.perform(get(APPLICATION_API_PATH, TEST_APPLICATION_NAME))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'If-None-Match' for method parameter type String is not present"));
    }

    @Test
    void testGetApplicationWithSameHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_APPLICATION_PATH);
        var dto = objectMapper.readValue(dtoJson, ApplicationDto.class);

        when(applicationFacade.getApplicationWithHash(eq(TEST_APPLICATION_NAME))).thenReturn(
                new DtoWithDomainHash<>(dto, "1"));

        mockMvc.perform(get(APPLICATION_API_PATH, TEST_APPLICATION_NAME)
                        .header(HEADER_IF_NONE_MATCH, "1"))
                .andExpect(status().isNotModified())
                .andExpect(header().exists("eTag"))
                .andExpect(header().string("eTag", "\"1\""));
    }

    @Test
    void testGetApplicationWithDifferentHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_APPLICATION_PATH);
        var dto = objectMapper.readValue(dtoJson, ApplicationDto.class);

        when(applicationFacade.getApplicationWithHash(eq(TEST_APPLICATION_NAME))).thenReturn(
                new DtoWithDomainHash<>(dto, "2"));

        mockMvc.perform(get(APPLICATION_API_PATH, TEST_APPLICATION_NAME)
                        .header(HEADER_IF_NONE_MATCH, "1"))
                .andExpect(status().isOk())
                .andExpect(header().exists("eTag"))
                .andExpect(header().string("eTag", "\"2\""))
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
    }


    @Test
    void testCreateApplication() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_APPLICATION_PATH);
        var dto = objectMapper.readValue(dtoJson, new TypeReference<ApplicationDto>() {
        });

        doNothing().when(applicationFacade).createApplication(eq(dto));

        mockMvc.perform(post(APPLICATION_BASE_API_PATH)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isNoContent());
    }

    @Test
    void testUpdateApplication() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_APPLICATION_PATH);
        var dto = objectMapper.readValue(dtoJson, new TypeReference<ApplicationDto>() {
        });

        when(applicationFacade.updateApplication(eq(TEST_APPLICATION_NAME), eq(dto), eq("1")))
                .thenReturn("2");

        mockMvc.perform(put(APPLICATION_API_PATH, TEST_APPLICATION_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HEADER_IF_MATCH, "1")
                        .content(dtoJson))
                .andExpect(status().isNoContent())
                .andExpect(header().exists("eTag"))
                .andExpect(header().string("eTag", "\"2\""));
        verify(applicationFacade).updateApplication(eq(TEST_APPLICATION_NAME), eq(dto), eq("1"));
    }

    @Test
    void testUpdateApplicationWithNotMatchHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_APPLICATION_PATH);
        var dto = objectMapper.readValue(dtoJson, ApplicationDto.class);

        doThrow(new OptimisticLockConflictException("Conflict Exception"))
                .when(applicationFacade).updateApplication(eq(TEST_APPLICATION_NAME), any(ApplicationDto.class), eq("1"));

        mockMvc.perform(put(APPLICATION_API_PATH, TEST_APPLICATION_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header("If-Match", "1")
                        .content(dtoJson))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.message").value("Conflict Exception"));
        verify(applicationFacade).updateApplication(eq(TEST_APPLICATION_NAME), eq(dto), eq("1"));
    }

    @Test
    void testUpdateApplicationWithoutHeaderIfMatch() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_APPLICATION_PATH);

        mockMvc.perform(put(APPLICATION_API_PATH, TEST_APPLICATION_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateApplication_appRouteWithInvalidPath_BadRequest() throws Exception {
        var dtoJson = ResourceUtils.readResource("/application_dto_with_invalid_app_route_path.json");

        when(applicationFacade.updateApplication(eq("test_application"), any(ApplicationDto.class), any())).thenReturn("test");

        mockMvc.perform(put("/api/v1/applications/{applicationName}", "test_application")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("paths[1].<list element>: Invalid route path. "
                        + "Path must be a valid plain path (starting with /) or a valid regular expression pattern (starting with / or ^/)")))
                .andExpect(jsonPath("$.message", containsString("paths[0].<list element>: Invalid route path. "
                        + "Path must be a valid plain path (starting with /) or a valid regular expression pattern (starting with / or ^/)")));
    }

    @Test
    void testDeleteApplication() throws Exception {

        doNothing().when(applicationFacade).deleteApplication(eq("test_application"));

        mockMvc.perform(delete("/api/v1/applications/{applicationName}", "test_application"))
                .andExpect(status().isNoContent());
    }

}