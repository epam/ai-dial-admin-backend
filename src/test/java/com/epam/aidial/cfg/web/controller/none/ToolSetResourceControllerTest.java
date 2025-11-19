package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.AuthenticationTypeResourceDto;
import com.epam.aidial.cfg.dto.CreateToolSetResourceDto;
import com.epam.aidial.cfg.dto.CredentialsLevelDto;
import com.epam.aidial.cfg.dto.ResourcePathDto;
import com.epam.aidial.cfg.dto.ResourcePathsDto;
import com.epam.aidial.cfg.dto.ResourceSignInRequestDto;
import com.epam.aidial.cfg.dto.ResourceSignOutRequestDto;
import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.NotModifiedException;
import com.epam.aidial.cfg.exception.ResourceNotFoundException;
import com.epam.aidial.cfg.exception.ResourcePreconditionFailedException;
import com.epam.aidial.cfg.mapper.ResourceMapperImpl;
import com.epam.aidial.cfg.mapper.ToolSetResourceMapperImpl;
import com.epam.aidial.cfg.model.CreateToolSetResource;
import com.epam.aidial.cfg.model.DomainModelWithEtag;
import com.epam.aidial.cfg.model.MoveResource;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.ToolSetResource;
import com.epam.aidial.cfg.model.ToolSetResourceNodeInfo;
import com.epam.aidial.cfg.service.ToolSetResourceService;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.ToolSetResourceController;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ToolSetResourceController.class)
@Import({
        JsonMapperConfiguration.class,
        ToolSetResourceMapperImpl.class,
        ResourceMapperImpl.class
})
public class ToolSetResourceControllerTest extends AbstractControllerNoneSecureTest {

    private static final String DTO_JSON_BASE_PATH = "/toolset-resources/";
    private static final String JSON_TOOLSET_CREATE_DTO = "toolset_create_dto.json";
    private static final String JSON_TOOLSET_CREATE = "toolset_create.json";
    private static final String APP_PATH = "rootPath/subFolder/TestName";
    private static final String APP_RESOURCE_BASE_API_PATH = "/api/v1/toolset-resources";
    private static final String GET_API_PATH = APP_RESOURCE_BASE_API_PATH + "/get";
    private static final String CREATE_API_PATH = APP_RESOURCE_BASE_API_PATH + "/create";
    private static final String UPDATE_API_PATH = APP_RESOURCE_BASE_API_PATH + "/update";
    private static final String DELETE_API_PATH = APP_RESOURCE_BASE_API_PATH + "/delete";
    private static final String DELETE_BULK_API_PATH = APP_RESOURCE_BASE_API_PATH + "/delete/bulk";
    private static final String LIST_API_PATH = APP_RESOURCE_BASE_API_PATH + "/list";
    private static final String MOVE_API_PATH = APP_RESOURCE_BASE_API_PATH + "/move";
    private static final String DISCOVERY_API_PATH = APP_RESOURCE_BASE_API_PATH + "/discovered-tools";
    private static final String TOOLSET_SIGN_IN = APP_RESOURCE_BASE_API_PATH + "/sign-in";
    private static final String TOOLSET_SIGN_OUT = APP_RESOURCE_BASE_API_PATH + "/sign-out";
    private static final String TEST_ETAG = "etag123";
    private static final String RETURNED_TEST_ETAG = "\"etag123\"";
    private static final String TOOLS_DTO_JSON_PATH = "/tools_dto.json";

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ToolSetResourceService toolSetResourceService;

    @Test
    void testGetAllToolSetResources() throws Exception {
        var toolSetsInfoRequestDtoJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + "toolset_infos_request_dto.json");

        var toolSetsInfoRequestJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + "toolset_infos_request.json");
        var toolSetsInfoRequest = objectMapper.readValue(toolSetsInfoRequestJson,
                new TypeReference<ResourceMetadataRequest>() {
                });

        var modelJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + "/toolset_infos.json");
        var model = objectMapper.readValue(modelJson, new TypeReference<ToolSetResourceNodeInfo>() {
        });

        when(toolSetResourceService.getToolSetResources(any())).thenReturn(model);

        var dtoJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + "toolset_infos_dto.json");
        mockMvc.perform(post(LIST_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toolSetsInfoRequestDtoJson))
                .andExpect(status().isOk())
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));

        verify(toolSetResourceService).getToolSetResources(eq(toolSetsInfoRequest));
    }

    @Test
    void testGetToolSetResource() throws Exception {
        var modelJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + "toolset_get.json");
        var model = objectMapper.readValue(modelJson, new TypeReference<ToolSetResource>() {
        });

        when(toolSetResourceService.getToolSetResource(any(), any()))
                .thenReturn(new DomainModelWithEtag<>(model, TEST_ETAG));

        var body = new ResourcePathDto();
        body.setPath(APP_PATH);
        var dtoJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + "toolset_get_dto.json");
        mockMvc.perform(post(GET_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header(HEADER_IF_NONE_MATCH, TEST_ETAG))
                .andExpect(status().isOk())
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));

        verify(toolSetResourceService).getToolSetResource(eq(APP_PATH), eq(TEST_ETAG));
    }

    @Test
    void testGetToolSet_whenResourceNotExist() throws Exception {
        var body = new ResourcePathDto();
        body.setPath(APP_PATH);

        doThrow(new ResourceNotFoundException("Not Found")).when(toolSetResourceService).getToolSetResource(any(), any());

        mockMvc.perform(post(GET_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header(HEADER_IF_NONE_MATCH, TEST_ETAG))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Not Found"));

        verify(toolSetResourceService).getToolSetResource(eq(APP_PATH), eq(TEST_ETAG));
    }

    @Test
    void testGetToolSet_whenResourceWithSameEtag() throws Exception {
        var body = new ResourcePathDto();
        body.setPath(APP_PATH);

        doThrow(new NotModifiedException(Map.of("etag", List.of(TEST_ETAG)))).when(toolSetResourceService).getToolSetResource(any(), any());

        mockMvc.perform(post(GET_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header(HEADER_IF_NONE_MATCH, TEST_ETAG))
                .andExpect(status().isNotModified())
                .andExpect(header().string(HEADER_ETAG, RETURNED_TEST_ETAG))
                .andExpect(content().string(""));

        verify(toolSetResourceService).getToolSetResource(eq(APP_PATH), eq(TEST_ETAG));
    }

    @Test
    void testGetToolSet_whenIfNoneMatchHeaderNotPresent() throws Exception {
        var body = new ResourcePathDto();
        body.setPath(APP_PATH);

        mockMvc.perform(post(GET_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'If-None-Match' for method parameter type String is not present"));
    }

    @Test
    void testCreateToolSetResource() throws Exception {

        var createToolSetDtoJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + JSON_TOOLSET_CREATE_DTO);
        var createToolSetJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + JSON_TOOLSET_CREATE);
        var createToolSet = objectMapper.readValue(createToolSetJson, new TypeReference<CreateToolSetResource>() {
        });

        when(toolSetResourceService.createToolSetResource(any())).thenReturn(TEST_ETAG);

        mockMvc.perform(post(CREATE_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createToolSetDtoJson))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HEADER_ETAG, RETURNED_TEST_ETAG));

        verify(toolSetResourceService).createToolSetResource(eq(createToolSet));
    }

    @Test
    void testCreateToolSetResource_whenResourcePresent() throws Exception {

        var createToolSetDtoJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + JSON_TOOLSET_CREATE_DTO);
        var createToolSetJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + JSON_TOOLSET_CREATE);
        var createToolSet = objectMapper.readValue(createToolSetJson, new TypeReference<CreateToolSetResource>() {
        });


        doThrow(new EntityAlreadyExistsException("Already exist"))
                .when(toolSetResourceService).createToolSetResource(any());

        mockMvc.perform(post(CREATE_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createToolSetDtoJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message")
                        .value("Already exist"));

        verify(toolSetResourceService).createToolSetResource(eq(createToolSet));
    }

    @Test
    void testCreateToolSetsResourceWithUndefinedDisplayName() throws Exception {
        var createToolSetDtoJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + JSON_TOOLSET_CREATE_DTO);

        // Test with null displayName
        var dtoNullDisplayName = objectMapper.readValue(createToolSetDtoJson, CreateToolSetResourceDto.class);
        dtoNullDisplayName.setDisplayName(null);
        var jsonNullDisplayName = objectMapper.writeValueAsString(dtoNullDisplayName);

        mockMvc.perform(post(CREATE_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonNullDisplayName))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("displayName: Display name is required"));
    }

    @Test
    void testCreateToolSetsResourceWithEmptyDisplayName() throws Exception {
        var createToolSetDtoJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + JSON_TOOLSET_CREATE_DTO);

        // Test with empty displayName
        var dtoEmptyDisplayName = objectMapper.readValue(createToolSetDtoJson, CreateToolSetResourceDto.class);
        dtoEmptyDisplayName.setDisplayName("");
        var jsonEmptyDisplayName = objectMapper.writeValueAsString(dtoEmptyDisplayName);

        mockMvc.perform(post(CREATE_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonEmptyDisplayName))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("displayName: Display name is required"));
    }

    @Test
    void testCreateToolSetsResourceWithEmptyEndpoint() throws Exception {
        var createToolSetDtoJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + JSON_TOOLSET_CREATE_DTO);

        // Test with empty endpoint
        var dtoEmptyEndpoint = objectMapper.readValue(createToolSetDtoJson, CreateToolSetResourceDto.class);
        dtoEmptyEndpoint.setEndpoint("");
        var jsonEmptyEndpoint = objectMapper.writeValueAsString(dtoEmptyEndpoint);

        mockMvc.perform(post(CREATE_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonEmptyEndpoint))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("endpoint: Completion endpoint is required"));
    }

    @Test
    void testCreateToolSetsResourceWithInvalidEndpoint() throws Exception {
        var createToolSetDtoJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + JSON_TOOLSET_CREATE_DTO);

        // Test with invalid endpoint URL
        var dtoInvalidEndpoint = objectMapper.readValue(createToolSetDtoJson, CreateToolSetResourceDto.class);
        dtoInvalidEndpoint.setEndpoint("invalid-url");
        var jsonInvalidEndpoint = objectMapper.writeValueAsString(dtoInvalidEndpoint);

        mockMvc.perform(post(CREATE_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInvalidEndpoint))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("endpoint: Invalid endpoint URL"));

        verifyNoInteractions(toolSetResourceService);
    }

    @Test
    void testUpdateToolSetResource() throws Exception {

        var updateToolSetDtoJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + JSON_TOOLSET_CREATE_DTO);
        var updateToolSetJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + JSON_TOOLSET_CREATE);
        var updateToolSet = objectMapper.readValue(updateToolSetJson, new TypeReference<CreateToolSetResource>() {
        });

        when(toolSetResourceService.putToolSetResource(any(), anyBoolean(), any())).thenReturn(TEST_ETAG);

        mockMvc.perform(post(UPDATE_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateToolSetDtoJson)
                        .header(HEADER_IF_MATCH, TEST_ETAG))
                .andExpect(status().isNoContent())
                .andExpect(header().string(HEADER_ETAG, RETURNED_TEST_ETAG));

        verify(toolSetResourceService).putToolSetResource(eq(updateToolSet), eq(true), eq(TEST_ETAG));
    }

    @Test
    void testUpdateToolSetResource_whenResourceNotExist() throws Exception {

        var updateToolSetDtoJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + JSON_TOOLSET_CREATE_DTO);
        var updateToolSetJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + JSON_TOOLSET_CREATE);
        var updateToolSet = objectMapper.readValue(updateToolSetJson, new TypeReference<CreateToolSetResource>() {
        });

        doThrow(new ResourceNotFoundException("Not Found"))
                .when(toolSetResourceService).putToolSetResource(any(), anyBoolean(), any());

        mockMvc.perform(post(UPDATE_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateToolSetDtoJson)
                        .header(HEADER_IF_MATCH, TEST_ETAG))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Not Found"));

        verify(toolSetResourceService).putToolSetResource(eq(updateToolSet), eq(true), eq(TEST_ETAG));
    }

    @Test
    void testUpdateToolSetResource_whenWrongIfMatchEtag() throws Exception {

        var updateToolSetDtoJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + JSON_TOOLSET_CREATE_DTO);
        var updateToolSetJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + JSON_TOOLSET_CREATE);
        var updateToolSet = objectMapper.readValue(updateToolSetJson, new TypeReference<CreateToolSetResource>() {
        });

        doThrow(new ResourcePreconditionFailedException("Precondition failed"))
                .when(toolSetResourceService).putToolSetResource(any(), anyBoolean(), any());

        mockMvc.perform(post(UPDATE_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateToolSetDtoJson)
                        .header(HEADER_IF_MATCH, TEST_ETAG))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.message")
                        .value("Precondition failed"));

        verify(toolSetResourceService).putToolSetResource(eq(updateToolSet), eq(true), eq(TEST_ETAG));
    }

    @Test
    void testUpdateToolSet_whenIfMatchHeaderNotPresent() throws Exception {
        var updateToolSetDtoJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + JSON_TOOLSET_CREATE_DTO);

        mockMvc.perform(post(UPDATE_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateToolSetDtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'If-Match' for method parameter type String is not present"));
    }

    @Test
    void testDeleteToolSet() throws Exception {
        var body = new ResourcePathDto();
        body.setPath(APP_PATH);
        mockMvc.perform(post(DELETE_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header(HEADER_IF_MATCH, "*"))
                .andExpect(status().isOk());

        verify(toolSetResourceService).deleteToolSetResource(APP_PATH, "*");
    }

    @Test
    void testDeleteToolSet_whenResourceNotExist() throws Exception {
        var body = new ResourcePathDto();
        body.setPath(APP_PATH);

        doThrow(new ResourceNotFoundException("Not Found")).when(toolSetResourceService).deleteToolSetResource(any(), any());

        mockMvc.perform(post(DELETE_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header(HEADER_IF_MATCH, TEST_ETAG))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Not Found"));

        verify(toolSetResourceService).deleteToolSetResource(APP_PATH, TEST_ETAG);
    }

    @Test
    void testDeleteToolSet_whenWrongEtag() throws Exception {
        var body = new ResourcePathDto();
        body.setPath(APP_PATH);

        doThrow(new ResourcePreconditionFailedException("Precondition failed")).when(toolSetResourceService).deleteToolSetResource(any(), any());

        mockMvc.perform(post(DELETE_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body))
                        .header(HEADER_IF_MATCH, TEST_ETAG))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.message")
                        .value("Precondition failed"));
    }

    @Test
    void testDeleteToolSet_whenIfMatchHeaderNotPresent() throws Exception {
        var body = new ResourcePathDto();
        body.setPath(APP_PATH);

        mockMvc.perform(post(DELETE_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'If-Match' for method parameter type String is not present"));

    }

    @Test
    void testDeleteToolSets() throws Exception {
        var path1 = "testPath1/TestName";
        var path2 = "testPath2/TestName";

        var pathDto1 = new ResourcePathDto();
        pathDto1.setPath(path1);

        var pathDto2 = new ResourcePathDto();
        pathDto2.setPath(path2);

        var pathsDto = new ResourcePathsDto();
        pathsDto.setPaths(List.of(pathDto1, pathDto2));

        mockMvc.perform(post(DELETE_BULK_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pathsDto)))
                .andExpect(status().isOk());

        verify(toolSetResourceService).deleteToolSetResources(List.of(path1, path2));
    }

    @Test
    void testMoveToolSet() throws Exception {
        var moveDtoJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + "move_toolset_dto.json");

        var moveJson = ResourceUtils.readResource(DTO_JSON_BASE_PATH + "move_toolset.json");
        var moveToolSet = objectMapper.readValue(moveJson, new TypeReference<MoveResource>() {
        });

        mockMvc.perform(post(MOVE_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(moveDtoJson))
                .andExpect(status().isOk());

        verify(toolSetResourceService).move(moveToolSet);
    }

    @Test
    void testDiscoverTools() throws Exception {
        var dtoJson = ResourceUtils.readResource(TOOLS_DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, new TypeReference<McpSchema.ListToolsResult>() {
        });

        when(toolSetResourceService.getDiscoveredTools(any(), any())).thenReturn(dto);

        var body = new ResourcePathDto();
        body.setPath(APP_PATH);
        mockMvc.perform(post(DISCOVERY_API_PATH)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testSignIn() throws Exception {
        var request = new ResourceSignInRequestDto();
        request.setUrl("testUrl");
        request.setAuthenticationType(AuthenticationTypeResourceDto.OAUTH);
        request.setCredentialsLevel(CredentialsLevelDto.GLOBAL);

        mockMvc.perform(post(TOOLSET_SIGN_IN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testSignIn_whenInvalidResourceSignInRequestDto() throws Exception {
        var request = new ResourceSignInRequestDto();
        request.setUrl("testUrl");
        request.setAuthenticationType(AuthenticationTypeResourceDto.OAUTH);

        mockMvc.perform(post(TOOLSET_SIGN_IN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("credentialsLevel: Credentials level should be specified"));
    }

    @Test
    void testSignOut() throws Exception {
        var request = new ResourceSignOutRequestDto();
        request.setUrl("testUrl");
        request.setAuthenticationType(AuthenticationTypeResourceDto.OAUTH);
        request.setCredentialsLevel(CredentialsLevelDto.GLOBAL);

        mockMvc.perform(post(TOOLSET_SIGN_OUT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testSignOut_whenInvalidResourceSignInRequestDto() throws Exception {
        var request = new ResourceSignInRequestDto();
        request.setUrl("testUrl");
        request.setAuthenticationType(AuthenticationTypeResourceDto.OAUTH);

        mockMvc.perform(post(TOOLSET_SIGN_IN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("credentialsLevel: Credentials level should be specified"));
    }
}