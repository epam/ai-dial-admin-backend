package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.dto.route.RouteDto;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = RouteController.class)
@Import({
        JsonMapperConfiguration.class,
})
class RouteControllerTest extends AbstractControllerNoneSecureTest {
    private static final String DTO_JSON_PATH = "/route_dto.json";
    private static final String TEST_ROUTE_NAME = "test_route";
    private static final String ROUTE_BASE_API_PATH = "/api/v1/routes";
    private static final String ROUTE_API_PATH = ROUTE_BASE_API_PATH + "/{routeName}";

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
        mockMvc.perform(get(ROUTE_BASE_API_PATH))
                //then
                .andExpect(status().isOk())
                .andExpect(content().json(dtosJson, JsonCompareMode.LENIENT));

    }

    @Test
    void testGetRouteWithoutHeaderIfNoneMatch() throws Exception {
        mockMvc.perform(get(ROUTE_API_PATH, TEST_ROUTE_NAME))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'If-None-Match' for method parameter type String is not present"));
    }

    @Test
    void testGetRouteWithSameHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, new TypeReference<RouteDto>() {
        });

        when(routeFacade.getRouteWithHash(eq(TEST_ROUTE_NAME))).thenReturn(
                new DtoWithDomainHash<>(dto, "1"));

        mockMvc.perform(get(ROUTE_API_PATH, TEST_ROUTE_NAME)
                        .header(HEADER_IF_NONE_MATCH, "1"))
                .andExpect(status().isNotModified())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"1\""));
    }

    @Test
    void testGetRouteWithDifferentHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, new TypeReference<RouteDto>() {
        });

        when(routeFacade.getRouteWithHash(eq(TEST_ROUTE_NAME))).thenReturn(
                new DtoWithDomainHash<>(dto, "2"));

        mockMvc.perform(get(ROUTE_API_PATH, TEST_ROUTE_NAME)
                        .header(HEADER_IF_NONE_MATCH, "1"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"2\""))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testCreateRoute() throws Exception {
        // given
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);

        doNothing().when(routeFacade).createRoute(any());
        // when
        mockMvc.perform(post(ROUTE_BASE_API_PATH)
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
        mockMvc.perform(post(ROUTE_BASE_API_PATH)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                // then
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateRoute_WithInvalidPath_BadRequest() throws Exception {
        // given
        var dtoJson = ResourceUtils.readResource("/route_dto_with_invalid_path.json");

        doNothing().when(routeFacade).createRoute(any());

        // when
        mockMvc.perform(post("/api/v1/routes")
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid route path")));
    }

    @Test
    void testCreateRoute_WithEmptyPathString_Success() throws Exception {
        // given
        var dtoJson = ResourceUtils.readResource("/route_dto_with_empty_path_string.json");

        doNothing().when(routeFacade).createRoute(any());

        // when
        mockMvc.perform(post(ROUTE_BASE_API_PATH)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                // then
                .andExpect(status().isNoContent());
    }

    @Test
    void testCreateRoute_WithBlankPathString_BadRequest() throws Exception {
        // given
        var dtoJson = ResourceUtils.readResource("/route_dto_with_blank_path_string.json");

        doNothing().when(routeFacade).createRoute(any());

        // when
        mockMvc.perform(post(ROUTE_BASE_API_PATH)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid route path")));
    }

    @Test
    void testUpdateRoute() throws Exception {
        // given
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);

        when(routeFacade.updateRoute(eq(TEST_ROUTE_NAME), any(RouteDto.class), eq("1")))
                .thenReturn("2");
        // when
        mockMvc.perform(put(ROUTE_API_PATH, TEST_ROUTE_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HEADER_IF_MATCH, "1")
                        .content(dtoJson))
                // then
                .andExpect(status().isNoContent())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"2\""));
    }

    @Test
    void testUpdateRouteWithNotMatchHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, new TypeReference<RouteDto>() {
        });

        doThrow(new OptimisticLockConflictException("Conflict Exception"))
                .when(routeFacade).updateRoute(eq(TEST_ROUTE_NAME), any(RouteDto.class), eq("1"));

        mockMvc.perform(put(ROUTE_API_PATH, TEST_ROUTE_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HEADER_IF_MATCH, "1")
                        .content(dtoJson))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.message").value("Conflict Exception"));
        verify(routeFacade).updateRoute(eq(TEST_ROUTE_NAME), eq(dto), eq("1"));
    }

    @Test
    void testUpdateRouteWithoutHeaderIfMatch() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);

        mockMvc.perform(put(ROUTE_API_PATH, TEST_ROUTE_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'If-Match' for method parameter type String is not present"));
    }

    @Test
    void testUpdateRoute_WithEmptyPathString_Success() throws Exception {
        // given
        var dtoJson = ResourceUtils.readResource("/route_dto_with_empty_path_string.json");

        when(routeFacade.updateRoute(eq(TEST_ROUTE_NAME), any(RouteDto.class), eq("1")))
                .thenReturn("2");

        // when
        mockMvc.perform(put(ROUTE_API_PATH, TEST_ROUTE_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HEADER_IF_MATCH, "1")
                        .content(dtoJson))
                // then
                .andExpect(status().isNoContent())
                .andExpect(header().exists(HEADER_ETAG));
    }

    @Test
    void testUpdateRoute_WithBlankPathString_BadRequest() throws Exception {
        // given
        var dtoJson = ResourceUtils.readResource("/route_dto_with_blank_path_string.json");

        // Validation will fail before facade is called, but we need to mock the return value
        when(routeFacade.updateRoute(any(String.class), any(RouteDto.class), any(String.class)))
                .thenReturn("2");

        // when
        mockMvc.perform(put(ROUTE_API_PATH, TEST_ROUTE_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HEADER_IF_MATCH, "1")
                        .content(dtoJson))
                // then
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("Invalid route path")));
    }

    @Test
    void testDeleteRoute() throws Exception {
        // given
        doNothing().when(routeFacade).deleteRoute(any());
        // when
        mockMvc.perform(delete(ROUTE_API_PATH, TEST_ROUTE_NAME))
                // then
                .andExpect(status().isNoContent());
    }
}