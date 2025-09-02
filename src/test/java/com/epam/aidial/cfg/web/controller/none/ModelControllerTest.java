package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.dto.ModelDto;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.ModelController;
import com.epam.aidial.cfg.web.facade.ModelFacade;
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

@WebMvcTest(controllers = ModelController.class)
@Import({
        JsonMapperConfiguration.class,
})
class ModelControllerTest extends AbstractControllerNoneSecureTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ModelFacade modelFacade;

    @Test
    void testGetAllModels() throws Exception {
        var dtosJson = ResourceUtils.readResource("/model_info_dtos.json");
        var dtos = objectMapper.readValue(dtosJson, new TypeReference<List<ModelDto>>() {
        });

        when(modelFacade.getAll()).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/models"))
                .andExpect(status().isOk())
                .andExpect(content().json(dtosJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testGetModelWithoutHeaderIfNoneMatch() throws Exception {
        mockMvc.perform(get("/api/v1/models/{modelName}", "test_model"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'If-None-Match' for method parameter type String is not present"));
    }

    @Test
    void testGetModelWithSameHash() throws Exception {
        var dtoJson = ResourceUtils.readResource("/model_dto.json");
        var dto = objectMapper.readValue(dtoJson, ModelDto.class);

        when(modelFacade.getModelWithHash(eq("test_model"))).thenReturn(
                new DtoWithDomainHash<>(dto, "1"));

        mockMvc.perform(get("/api/v1/models/{modelName}", "test_model")
                        .header("If-None-Match", "1"))
                .andExpect(status().isNotModified())
                .andExpect(header().exists("eTag"))
                .andExpect(header().string("eTag", "\"1\""));
    }

    @Test
    void testGetModeWithDifferentHash() throws Exception {
        var dtoJson = ResourceUtils.readResource("/model_dto.json");
        var dto = objectMapper.readValue(dtoJson, ModelDto.class);

        when(modelFacade.getModelWithHash(eq("test_model"))).thenReturn(
                new DtoWithDomainHash<>(dto, "2"));

        mockMvc.perform(get("/api/v1/models/{modelName}", "test_model")
                        .header("If-None-Match", "1"))
                .andExpect(status().isOk())
                .andExpect(header().exists("eTag"))
                .andExpect(header().string("eTag", "\"2\""))
                .andExpect(content().contentType("application/json"))
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testCreateModel() throws Exception {
        var dtoJson = ResourceUtils.readResource("/model_dto.json");
        var dto = objectMapper.readValue(dtoJson, new TypeReference<ModelDto>() {
        });


        mockMvc.perform(post("/api/v1/models")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isNoContent());
        verify(modelFacade).createModel(eq(dto));
    }

    @Test
    void testUpdateModel() throws Exception {
        var dtoJson = ResourceUtils.readResource("/model_dto.json");
        var dto = objectMapper.readValue(dtoJson, ModelDto.class);

        when(modelFacade.updateModel(eq("test_model"), any(), eq("1"))).thenReturn(
                "2");

        mockMvc.perform(put("/api/v1/models/{modelName}", "test_model")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header("If-Match", "1")
                        .content(dtoJson))
                .andExpect(status().isNoContent())
                .andExpect(header().exists("eTag"))
                .andExpect(header().string("eTag", "\"2\""));
        verify(modelFacade).updateModel(eq("test_model"), eq(dto), eq("1"));
    }

    @Test
    void testUpdateModelWithNotMatchHash() throws Exception {
        var dtoJson = ResourceUtils.readResource("/model_dto.json");
        var dto = objectMapper.readValue(dtoJson, ModelDto.class);

        doThrow(new OptimisticLockConflictException("Conflict Exception"))
                .when(modelFacade).updateModel(eq("test_model"), any(), eq("1"));

        mockMvc.perform(put("/api/v1/models/{modelName}", "test_model")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header("If-Match", "1")
                        .content(dtoJson))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.message").value("Conflict Exception"));
        verify(modelFacade).updateModel(eq("test_model"), eq(dto), eq("1"));
    }

    @Test
    void testUpdateModelWithoutHeaderIfMatch() throws Exception {
        var dtoJson = ResourceUtils.readResource("/model_dto.json");

        mockMvc.perform(put("/api/v1/models/{modelName}", "test_model")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testDeleteModel() throws Exception {


        mockMvc.perform(delete("/api/v1/models/{modelName}", "test_model"))
                .andExpect(status().isNoContent());
        verify(modelFacade).deleteModel(eq("test_model"));
    }

    @Test
    void testCreateModel_EmptyUpstreamEndpoint_ValidationException() throws Exception {
        var dtoJson = ResourceUtils.readResource("/model_dto_with_empty_upstream_endpoint.json");

        mockMvc.perform(post("/api/v1/models")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("JSON parse error: endpoint: Invalid upstream endpoint"));
    }
}