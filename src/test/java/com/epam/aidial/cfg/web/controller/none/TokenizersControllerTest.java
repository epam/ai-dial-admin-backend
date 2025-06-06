package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.TokenizerDto;
import com.epam.aidial.cfg.service.TokenizerService;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.TokenizersController;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = TokenizersController.class)
@Import({
    JsonMapperConfiguration.class,
})
class TokenizersControllerTest extends AbstractControllerNoneSecureTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TokenizerService tokenizerService;

    @Test
    void testGetAllKeys() throws Exception {
        var dtosJson = ResourceUtils.readResource("/tokenizer_dtos.json");
        var dtos = objectMapper.readValue(dtosJson, new TypeReference<List<TokenizerDto>>() {
        });

        when(tokenizerService.getAllTokenizers()).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/tokenizers"))
                .andExpect(status().isOk())
                .andExpect(content().json(dtosJson, JsonCompareMode.LENIENT));
    }
}
