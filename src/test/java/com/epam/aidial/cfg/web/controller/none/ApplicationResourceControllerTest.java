package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.client.mapper.RouteMapperImpl;
import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.ResourcePathDto;
import com.epam.aidial.cfg.mapper.ApplicationResourceMapperImpl;
import com.epam.aidial.cfg.mapper.PublicationMapperImpl;
import com.epam.aidial.cfg.mapper.ResourceMapperImpl;
import com.epam.aidial.cfg.model.ApplicationResource;
import com.epam.aidial.cfg.model.CreateApplicationResource;
import com.epam.aidial.cfg.service.ApplicationResourceService;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.ApplicationResourceController;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ApplicationResourceController.class)
@Import({
        JsonMapperConfiguration.class,
        ApplicationResourceMapperImpl.class,
        RouteMapperImpl.class,
        ResourceMapperImpl.class
})
class ApplicationResourceControllerTest extends AbstractControllerNoneSecureTest {
    private static final String DTO_JSON_BASE_PATH = "/application-resources/";
    private static final String APP_PATH = "rootPath/subFolder/TestName";
    private static final String APP_RESOURCE_BASE_API_PATH = "/api/v1/application-resources";
    private static final String GET_API_PATH = APP_RESOURCE_BASE_API_PATH + "/get";
    private static final String CREATE_API_PATH = APP_RESOURCE_BASE_API_PATH + "/create";
    private static final String DELETE_API_PATH = APP_RESOURCE_BASE_API_PATH + "/delete";
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ApplicationResourceService applicationResourceService;

    public static Stream<Arguments> createApplicationParams() {
        return Stream.of(
                Arguments.of("Create with schema and app.properties", "app_create_ws_dto.json", "app_create_ws.json", "app_created_ws.json", "app_created_ws_dto.json"),
                Arguments.of("Create without schema", "app_create_wo_dto.json", "app_create_wo.json", "app_created_wo.json", "app_created_wo_dto.json")
        );
    }

    public static Stream<Arguments> getApplicationParams() {
        return Stream.of(
                Arguments.of("Get application with schema", "app_get_ws.json", "app_get_ws_dto.json"),
                Arguments.of("Get application without schema", "app_get_wo.json", "app_get_wo_dto.json")
        );
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("getApplicationParams")
    void testGetApplicationResource(String testName,
                                                 String pathAppDtoJson,
                                                 String pathAppJson) throws Exception {
        var modelJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + pathAppDtoJson);
        var model = objectMapper.readValue(modelJson, new TypeReference<ApplicationResource>() {
        });

        when(applicationResourceService.getApplicationResource(any())).thenReturn(model);

        var body = new ResourcePathDto();
        body.setPath(APP_PATH);
        var dtoJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + pathAppJson);
        mockMvc.perform(post(GET_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));

        verify(applicationResourceService).getApplicationResource(eq(APP_PATH));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("createApplicationParams")
    void testCreateApplicationResource(String testName,
                                       String pathCreateAppDtoJson,
                                       String pathCreateAppJson,
                                       String pathCreatedAppJson,
                                       String pathCreatedAppDtoJson) throws Exception {

        var createApplicationDtoJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + pathCreateAppDtoJson);
        var createApplicationJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + pathCreateAppJson);
        var createApplication = objectMapper.readValue(createApplicationJson, new TypeReference<CreateApplicationResource>() {
        });
        var createdApplicationJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + pathCreatedAppJson);
        var createdApplication = objectMapper.readValue(createdApplicationJson, new TypeReference<ApplicationResource>() {
        });
        var createdApplicationDtoJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + pathCreatedAppDtoJson);

        when(applicationResourceService.createApplicationResource(any(), anyBoolean(), any())).thenReturn(createdApplication);

        mockMvc.perform(post(CREATE_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createApplicationDtoJson))
                .andExpect(status().isOk())
                .andExpect(content().json(createdApplicationDtoJson, JsonCompareMode.LENIENT));

        verify(applicationResourceService).createApplicationResource(eq(createApplication), eq(true), isNull());
    }

    @Test
    void testDeleteApplication() throws Exception {
        var body = new ResourcePathDto();
        body.setPath(APP_PATH);
        mockMvc.perform(post(DELETE_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(applicationResourceService).deleteApplicationResource(APP_PATH);
    }

}