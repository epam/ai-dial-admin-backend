package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.ApplicationTypeSchemaDto;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ApplicationTypeSchemaController.class)
@Import({
        JsonMapperConfiguration.class,
})
class ApplicationTypeSchemaControllerTest extends AbstractControllerNoneSecureTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApplicationTypeSchemaFacade schemaFacade;

    @Test
    void testGetAll() throws Exception {
        // given
        var dtosJson = ResourceUtils.readResource("/application_type_schema_dtos.json");
        var dtos = objectMapper.readValue(dtosJson, new TypeReference<List<ApplicationTypeSchemaDto>>() {
        });

        when(schemaFacade.getAll()).thenReturn(dtos);
        // when
        mockMvc.perform(get("/api/v1/applicationTypeSchemas"))
                //then
                .andExpect(status().isOk())
                .andExpect(content().json(dtosJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testGet() throws Exception {
        // given
        var dtoJson = ResourceUtils.readResource("/application_type_schema_dto.json");
        var expected = ResourceUtils.readResource("/application_type_schema_dtos.json");
        var dto = objectMapper.readValue(dtoJson, new TypeReference<ApplicationTypeSchemaDto>() {
        });

        when(schemaFacade.get(eq("test-schema"))).thenReturn(dto);
        // when
        mockMvc.perform(get("/api/v1/applicationTypeSchemas")
                        .param("id", "test-schema"))
                // then
                .andExpect(status().isOk())
                .andExpect(content().json(expected, JsonCompareMode.LENIENT));
    }

    @Test
    void testCreate() throws Exception {
        // given
        var dtoJson = ResourceUtils.readResource("/application_type_schema_dto.json");

        doNothing().when(schemaFacade).create(any());
        // when
        mockMvc.perform(post("/api/v1/applicationTypeSchemas")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                // then
                .andExpect(status().isNoContent());
    }

    @Test
    void testUpdate() throws Exception {
        // given
        var dtoJson = ResourceUtils.readResource("/application_type_schema_dto.json");

        doNothing().when(schemaFacade).update(any(), any());
        // when
        mockMvc.perform(put("/api/v1/applicationTypeSchemas")
                        .param("id", "test-schema")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                // then
                .andExpect(status().isNoContent());
    }

    @Test
    void testDelete() throws Exception {
        // given
        doNothing().when(schemaFacade).delete(any(), eq(false));
        // when
        mockMvc.perform(delete("/api/v1/applicationTypeSchemas")
                        .param("id", "test-schema"))
                // then
                .andExpect(status().isNoContent());
    }
}