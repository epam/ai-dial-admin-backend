package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.KeyDto;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.KeyController;
import com.epam.aidial.cfg.web.facade.KeyFacade;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = KeyController.class)
@Import({
        JsonMapperConfiguration.class,
})
class KeyControllerTest extends AbstractControllerNoneSecureTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private KeyFacade keyFacade;

    @Test
    void testGetAllKeys() throws Exception {

        var dtosJson = ResourceUtils.readResource("/key_dtos.json");
        var dtos = objectMapper.readValue(dtosJson, new TypeReference<List<KeyDto>>() {
        });

        when(keyFacade.getAllKeys()).thenReturn(dtos);

        mockMvc.perform(get("/api/v1/keys"))
                .andExpect(status().isOk())
                .andExpect(content().json(dtosJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testGetKey() throws Exception {

        var dtoJson = ResourceUtils.readResource("/key_dto.json");
        var dto = objectMapper.readValue(dtoJson, new TypeReference<KeyDto>() {
        });

        when(keyFacade.getKey(eq("test_key"))).thenReturn(dto);

        mockMvc.perform(get("/api/v1/keys/{keyName}", "test_key"))
                .andExpect(status().isOk())
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testCreateKey() throws Exception {

        var dtoJson = ResourceUtils.readResource("/key_dto.json");
        var dto = objectMapper.readValue(dtoJson, new TypeReference<KeyDto>() {
        });

        mockMvc.perform(post("/api/v1/keys")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isNoContent());

        verify(keyFacade).createKey(eq(dto));
    }

    @Test
    void testUpdateKey() throws Exception {

        var dtoJson = ResourceUtils.readResource("/key_dto.json");
        var dto = objectMapper.readValue(dtoJson, new TypeReference<KeyDto>() {
        });

        mockMvc.perform(put("/api/v1/keys/{keyName}", "test_key")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isNoContent());
        verify(keyFacade).updateKey(eq("test_key"), eq(dto));
    }

    @Test
    void testDeleteKey() throws Exception {


        mockMvc.perform(delete("/api/v1/keys/{keyName}", "test_key"))
                .andExpect(status().isNoContent());
        verify(keyFacade).deleteKey(eq("test_key"));
    }
}
