package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.AppConfiguration;
import com.epam.aidial.cfg.service.DescriptionKeywordsService;
import com.epam.aidial.cfg.web.controller.TopicController;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TopicController.class)
@Import({
    AppConfiguration.class,
})
class TopicControllerTest extends AbstractControllerNoneSecureTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DescriptionKeywordsService descriptionKeywordsService;

    @Test
    void testGetAllTopics() throws Exception {
        var topics = List.of("topic1", "topic2");

        when(descriptionKeywordsService.getAllDescriptionKeywords()).thenReturn(topics);

        mockMvc.perform(get("/api/v1/topics"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(topics)));
    }

}