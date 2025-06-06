package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.AssistantDto;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.AssistantController;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.json.JsonCompareMode;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = AssistantController.class)
@Import({
    JsonMapperConfiguration.class,
})
@Disabled
class AssistantControllerTest extends AbstractControllerNoneSecureTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testGetAllAssistants() throws Exception {
        var dtosJson = ResourceUtils.readResource("/assistant_dtos.json");
        var dtos = objectMapper.readValue(dtosJson, new TypeReference<List<AssistantDto>>() {
        });

        mockMvc.perform(get("/api/v1/assistants"))
                .andExpect(status().isOk())
                .andExpect(content().json(dtosJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testGetAssistant() throws Exception {
        var dtoJson = ResourceUtils.readResource("/assistant_dto.json");
        var dto = objectMapper.readValue(dtoJson, new TypeReference<AssistantDto>() {
        });

        mockMvc.perform(get("/api/v1/assistants/{assistantName}", "test_assistant"))
                .andExpect(status().isOk())
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testCreateAssistant() throws Exception {
        var dtoJson = ResourceUtils.readResource("/assistant_dto.json");
        var dto = objectMapper.readValue(dtoJson, new TypeReference<AssistantDto>() {
        });

        mockMvc.perform(post("/api/v1/assistants")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isNoContent());
    }

    @Test
    void testUpdateAssistant() throws Exception {
        var dtoJson = ResourceUtils.readResource("/assistant_dto.json");
        var dto = objectMapper.readValue(dtoJson, new TypeReference<AssistantDto>() {
        });

        mockMvc.perform(put("/api/v1/assistants/{assistantName}", "test_assistant")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteAssistant() throws Exception {

        mockMvc.perform(delete("/api/v1/assistants/{assistantName}", "test_assistant"))
                .andExpect(status().isNoContent());
    }
}
