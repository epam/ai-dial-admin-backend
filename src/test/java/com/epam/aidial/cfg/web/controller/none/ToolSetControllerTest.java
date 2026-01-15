package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.DtoWithDomainHash;
import com.epam.aidial.cfg.dto.ToolSetDto;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.ToolSetController;
import com.epam.aidial.cfg.web.facade.ToolSetFacade;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.modelcontextprotocol.spec.McpSchema;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ToolSetController.class)
@Import({
        JsonMapperConfiguration.class,
})
class ToolSetControllerTest extends AbstractControllerNoneSecureTest {

    private static final String DTO_JSON_PATH = "/tool_set_dto.json";
    private static final String TOOLS_DTO_JSON_PATH = "/tools_dto.json";
    private static final String CALL_TOOL_REQUEST_DTO_JSON_PATH = "/call_tool_request_dto.json";
    private static final String CALL_TOOL_RESULT_DTO_JSON_PATH = "/call_tool_result_dto.json";
    private static final String TEST_TOOL_SET_NAME = "test_tool_set";
    private static final String TOOL_SET_BASE_API_PATH = "/api/v1/toolSets";
    private static final String TOOL_SET_API_PATH = TOOL_SET_BASE_API_PATH + "/{toolSetName}";

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ToolSetFacade toolSetFacade;

    @Test
    void testGetAllToolSets() throws Exception {
        var dtosJson = ResourceUtils.readResource("/tool_set_dtos.json");
        var dtos = objectMapper.readValue(dtosJson, new TypeReference<List<ToolSetDto>>() {
        });

        when(toolSetFacade.getAllToolSets()).thenReturn(dtos);

        mockMvc.perform(get(TOOL_SET_BASE_API_PATH))
                .andExpect(status().isOk())
                .andExpect(content().json(dtosJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testGetToolSetWithoutHeaderIfNoneMatch() throws Exception {
        mockMvc.perform(get(TOOL_SET_API_PATH, TEST_TOOL_SET_NAME))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'If-None-Match' for method parameter type String is not present"));
    }

    @Test
    void testGetToolSetWithSameHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, new TypeReference<ToolSetDto>() {
        });

        when(toolSetFacade.getToolSetWithHash(eq(TEST_TOOL_SET_NAME))).thenReturn(
                new DtoWithDomainHash<>(dto, "1"));

        mockMvc.perform(get(TOOL_SET_API_PATH, TEST_TOOL_SET_NAME)
                        .header(HEADER_IF_NONE_MATCH, "1"))
                .andExpect(status().isNotModified())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"1\""));
    }

    @Test
    void testGetToolSetWithDifferentHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, new TypeReference<ToolSetDto>() {
        });

        when(toolSetFacade.getToolSetWithHash(eq(TEST_TOOL_SET_NAME))).thenReturn(
                new DtoWithDomainHash<>(dto, "2"));

        mockMvc.perform(get(TOOL_SET_API_PATH, TEST_TOOL_SET_NAME)
                        .header(HEADER_IF_NONE_MATCH, "1"))
                .andExpect(status().isOk())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"2\""))
                .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
    }


    @Test
    void testCreateToolSet() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, new TypeReference<ToolSetDto>() {
        });

        doNothing().when(toolSetFacade).createToolSet(eq(dto));

        mockMvc.perform(post(TOOL_SET_BASE_API_PATH)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isNoContent());
    }

    @Test
    void testUpdateToolSet() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, ToolSetDto.class);

        when(toolSetFacade.updateToolSet(eq(TEST_TOOL_SET_NAME), any(ToolSetDto.class), eq("1")))
                .thenReturn("2");

        mockMvc.perform(put(TOOL_SET_API_PATH, TEST_TOOL_SET_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HEADER_IF_MATCH, "1")
                        .content(dtoJson))
                .andExpect(status().isNoContent())
                .andExpect(header().exists(HEADER_ETAG))
                .andExpect(header().string(HEADER_ETAG, "\"2\""));
        verify(toolSetFacade).updateToolSet(eq(TEST_TOOL_SET_NAME), eq(dto), eq("1"));
    }

    @Test
    void testUpdateToolSetWithNotMatchHash() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, ToolSetDto.class);

        doThrow(new OptimisticLockConflictException("Conflict Exception"))
                .when(toolSetFacade).updateToolSet(eq(TEST_TOOL_SET_NAME), any(ToolSetDto.class), eq("1"));

        mockMvc.perform(put(TOOL_SET_API_PATH, TEST_TOOL_SET_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .header(HEADER_IF_MATCH, "1")
                        .content(dtoJson))
                .andExpect(status().isPreconditionFailed())
                .andExpect(jsonPath("$.message").value("Conflict Exception"));
        verify(toolSetFacade).updateToolSet(eq(TEST_TOOL_SET_NAME), eq(dto), eq("1"));
    }

    @Test
    void testUpdateToolSetWithoutHeaderIfMatch() throws Exception {
        var dtoJson = ResourceUtils.readResource(DTO_JSON_PATH);

        mockMvc.perform(put(TOOL_SET_API_PATH, TEST_TOOL_SET_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(dtoJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("Required request header 'If-Match' for method parameter type String is not present"));
    }

    @Test
    void testDeleteToolSet() throws Exception {
        doNothing().when(toolSetFacade).deleteToolSet(eq(TEST_TOOL_SET_NAME));

        mockMvc.perform(delete(TOOL_SET_API_PATH, TEST_TOOL_SET_NAME))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDiscoverTools() throws Exception {
        var dtoJson = ResourceUtils.readResource(TOOLS_DTO_JSON_PATH);
        var dto = objectMapper.readValue(dtoJson, new TypeReference<McpSchema.ListToolsResult>() {
        });

        when(toolSetFacade.getDiscoveredTools(eq(TEST_TOOL_SET_NAME), eq(null))).thenReturn(dto);

        mockMvc.perform(get(TOOL_SET_API_PATH + "/discovered-tools", TEST_TOOL_SET_NAME))
                .andExpect(status().isOk())
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testCallTool() throws Exception {
        var callToolRequestDtoJson = ResourceUtils.readResource(CALL_TOOL_REQUEST_DTO_JSON_PATH);
        var callToolRequestDto = objectMapper.readValue(callToolRequestDtoJson, new TypeReference<McpSchema.CallToolRequest>() {
        });

        var callToolResultDtoJson = ResourceUtils.readResource(CALL_TOOL_RESULT_DTO_JSON_PATH);
        var callToolResultDto = objectMapper.readValue(callToolResultDtoJson, new TypeReference<McpSchema.CallToolResult>() {
        });

        when(toolSetFacade.callTool(TEST_TOOL_SET_NAME, callToolRequestDto)).thenReturn(callToolResultDto);

        mockMvc.perform(post(TOOL_SET_API_PATH + "/call-tool", TEST_TOOL_SET_NAME)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .content(callToolRequestDtoJson))
                .andExpect(status().isOk())
                .andExpect(content().json(callToolResultDtoJson, JsonCompareMode.LENIENT));
    }

}