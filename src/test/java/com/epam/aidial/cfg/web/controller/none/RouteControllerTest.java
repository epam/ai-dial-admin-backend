package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.route.RouteDto;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.RouteController;
import com.epam.aidial.cfg.web.facade.RouteFacade;
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

@WebMvcTest(controllers = RouteController.class)
@Import({
        JsonMapperConfiguration.class,
})
class RouteControllerTest extends AbstractControllerNoneSecureTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RouteFacade routeFacade;

    @Test
    void testGetAllRoutes() throws Exception {
        // given
        var dtosJson = ResourceUtils.readResource("/route_dtos.json");
        var dtos = objectMapper.readValue(dtosJson, new TypeReference<List<RouteDto>>() {
        });

        when(routeFacade.getAllRoutes()).thenReturn(dtos);
        // when
        mockMvc.perform(get("/api/v1/routes"))
                //then
                .andExpect(status().isOk())
                .andExpect(content().json(dtosJson, JsonCompareMode.LENIENT));

    }

    @Test
    void testGetRoute() throws Exception {
        // given
        var dtoJson = ResourceUtils.readResource("/route_dto.json");
        var dto = objectMapper.readValue(dtoJson, new TypeReference<RouteDto>() {
        });

        when(routeFacade.getRoute(eq("test_route"))).thenReturn(dto);
        // when
        mockMvc.perform(get("/api/v1/routes/{routeName}", "test_route"))
                // then
                .andExpect(status().isOk())
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testCreateRoute() throws Exception {
        // given
        var dtoJson = ResourceUtils.readResource("/route_dto.json");

        doNothing().when(routeFacade).createRoute(any());
        // when
        mockMvc.perform(post("/api/v1/routes")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                // then
                .andExpect(status().isNoContent());
    }

    @Test
    void testCreateRoute_WithEmptyPaths_BadRequest() throws Exception {
        // given
        var dtoJson = ResourceUtils.readResource("/route_dto_with_empty_paths.json");

        doNothing().when(routeFacade).createRoute(any());
        // when
        mockMvc.perform(post("/api/v1/routes")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                // then
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateRoute() throws Exception {
        // given
        var dtoJson = ResourceUtils.readResource("/route_dto.json");

        doNothing().when(routeFacade).updateRoute(any(), any());
        // when
        mockMvc.perform(put("/api/v1/routes/{routeName}", "test_route")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                // then
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteRoute() throws Exception {
        // given
        doNothing().when(routeFacade).deleteRoute(any());
        // when
        mockMvc.perform(delete("/api/v1/routes/{routeName}", "test_route"))
                // then
                .andExpect(status().isNoContent());
    }
}