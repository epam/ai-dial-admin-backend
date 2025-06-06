package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.ModelDto;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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

        when(modelFacade.getAllModels()).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/models"))
                .andExpect(status().isOk())
                .andExpect(content().json(dtosJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testGetModel() throws Exception {
        var dtoJson = ResourceUtils.readResource("/model_dto.json");
        var dto = objectMapper.readValue(dtoJson, new TypeReference<ModelDto>() {
        });

        when(modelFacade.getModel(eq("test_model"))).thenReturn(dto);

        mockMvc.perform(get("/api/v1/models/{modelName}", "test_model"))
                .andExpect(status().isOk())
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
        var dto = objectMapper.readValue(dtoJson, new TypeReference<ModelDto>() {
        });


        mockMvc.perform(put("/api/v1/models/{modelName}", "test_model")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isNoContent());
        verify(modelFacade).updateModel(eq("test_model"), eq(dto));
    }

    @Test
    void testDeleteModel() throws Exception {


        mockMvc.perform(delete("/api/v1/models/{modelName}", "test_model"))
                .andExpect(status().isNoContent());
        verify(modelFacade).deleteModel(eq("test_model"));
    }

    @Test
    void testUpdateModel_RoleLimitsWithDefault_ValidationException() throws Exception {
        var dtoJson = ResourceUtils.readResource("/model_dto_role_limits_with_default.json");

        mockMvc.perform(put("/api/v1/models/{modelName}", "test_model")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isBadRequest())

                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.message").value("roleLimits: The role limits cannot contain the default role name."));
    }

}