package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.ApplicationDto;
import com.epam.aidial.cfg.dto.ApplicationInfoDto;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ApplicationController.class)
@Import({
        JsonMapperConfiguration.class,
})
class ApplicationControllerTest extends AbstractControllerNoneSecureTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApplicationFacade applicationFacade;

    @Test
    void testGetAllApplications() throws Exception {
        var dtosJson = ResourceUtils.readResource("/application_info_dtos.json");
        var dtos = objectMapper.readValue(dtosJson, new TypeReference<List<ApplicationInfoDto>>() {
        });

        when(applicationFacade.getAllApplications()).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/applications"))
                .andExpect(status().isOk())
                .andExpect(content().json(dtosJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testGetApplication() throws Exception {
        var dtoJson = ResourceUtils.readResource("/application_dto.json");
        var dto = objectMapper.readValue(dtoJson, new TypeReference<ApplicationDto>() {
        });

        when(applicationFacade.getApplication(eq("test_application"))).thenReturn(dto);

        mockMvc.perform(get("/api/v1/applications/{applicationName}", "test_application"))
                .andExpect(status().isOk())
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testCreateApplication() throws Exception {
        var dtoJson = ResourceUtils.readResource("/application_dto.json");
        var dto = objectMapper.readValue(dtoJson, new TypeReference<ApplicationDto>() {
        });

        doNothing().when(applicationFacade).createApplication(eq(dto));

        mockMvc.perform(post("/api/v1/applications")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isNoContent());
    }

    @Test
    void testUpdateApplication() throws Exception {
        var dtoJson = ResourceUtils.readResource("/application_dto.json");
        var dto = objectMapper.readValue(dtoJson, new TypeReference<ApplicationDto>() {
        });

        doNothing().when(applicationFacade).updateApplication(eq("test_application"), eq(dto));

        mockMvc.perform(put("/api/v1/applications/{applicationName}", "test_application")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteApplication() throws Exception {

        doNothing().when(applicationFacade).deleteApplication(eq("test_application"));

        mockMvc.perform(delete("/api/v1/applications/{applicationName}", "test_application"))
                .andExpect(status().isNoContent());
    }

}