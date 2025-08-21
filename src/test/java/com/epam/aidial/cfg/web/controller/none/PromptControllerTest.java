package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.ExportDto;
import com.epam.aidial.cfg.dto.ImportResourcesConflictResolutionStrategyDto;
import com.epam.aidial.cfg.dto.ImportResourcesDto;
import com.epam.aidial.cfg.dto.PromptPathDto;
import com.epam.aidial.cfg.dto.PromptPathsDto;
import com.epam.aidial.cfg.dto.PromptVersionsRequestDto;
import com.epam.aidial.cfg.dto.PromptsEximDto;
import com.epam.aidial.cfg.mapper.PromptMapperImpl;
import com.epam.aidial.cfg.mapper.ResourceMapperImpl;
import com.epam.aidial.cfg.model.CreatePrompt;
import com.epam.aidial.cfg.model.ImportConflictResolutionStrategy;
import com.epam.aidial.cfg.model.ImportResourcePreview;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ImportResourcesFileResult;
import com.epam.aidial.cfg.model.ImportResourcesPreview;
import com.epam.aidial.cfg.model.ImportResourcesResult;
import com.epam.aidial.cfg.model.MoveResource;
import com.epam.aidial.cfg.model.Prompt;
import com.epam.aidial.cfg.model.PromptNodeInfo;
import com.epam.aidial.cfg.model.PromptsExim;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.service.prompt.PromptEximService;
import com.epam.aidial.cfg.service.prompt.PromptService;
import com.epam.aidial.cfg.service.prompt.ZipPromptEximService;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.PromptsController;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.util.MimeTypeUtils;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = PromptsController.class)
@Import({
        JsonMapperConfiguration.class,
        PromptMapperImpl.class,
        ResourceMapperImpl.class
})
class PromptControllerTest extends AbstractControllerNoneSecureTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PromptService promptService;
    @MockitoBean
    private PromptEximService promptEximService;
    @MockitoBean
    private ZipPromptEximService zipPromptEximService;

    @Test
    void testGetAllPrompts() throws Exception {
        var promptsInfoRequestDtoJson = ResourceUtils.readResource("/prompts/prompt_infos_request_dto.json");

        var promptsInfoRequestJson = ResourceUtils.readResource("/prompts/prompt_infos_request.json");
        var promptsInfoRequest = objectMapper.readValue(promptsInfoRequestJson, new TypeReference<ResourceMetadataRequest>() {
        });

        var modelJson = ResourceUtils.readResource("/prompts/prompt_infos.json");
        var model = objectMapper.readValue(modelJson, new TypeReference<PromptNodeInfo>() {
        });

        when(promptService.getPrompts(any())).thenReturn(model);

        var dtoJson = ResourceUtils.readResource("/prompts/prompt_infos_dto.json");
        mockMvc.perform(post("/api/v1/prompts/list")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(promptsInfoRequestDtoJson))
                .andExpect(status().isOk())
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));

        verify(promptService).getPrompts(eq(promptsInfoRequest));
    }

    @Test
    void testGetPrompt() throws Exception {
        var modelJson = ResourceUtils.readResource("/prompts/prompt.json");
        var model = objectMapper.readValue(modelJson, new TypeReference<Prompt>() {
        });
        var promptPath = "rootPath/subFolder/TestName";

        when(promptService.getPrompt(any())).thenReturn(model);

        var body = new PromptPathDto();
        body.setPath(promptPath);
        var dtoJson = ResourceUtils.readResource("/prompts/prompt_dto.json");
        mockMvc.perform(post("/api/v1/prompts/get")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));

        verify(promptService).getPrompt(eq(promptPath));
    }

    @Test
    void testGetPrompt_PathIsNull_ThrowValidationError() throws Exception {
        var body = new PromptPathDto();
        body.setPath(null);
        mockMvc.perform(post("/api/v1/prompts/get")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("path: must not be empty"));

        verifyNoInteractions(promptService);
    }

    @Test
    void testGetPromptVersions() throws Exception {
        var modelJson = ResourceUtils.readResource("/prompts/prompt_versions_info.json");
        var model = objectMapper.readValue(modelJson, new TypeReference<List<PromptNodeInfo>>() {
        });
        var folderId = "rootPath/subFolder";
        var name = "TestName";

        when(promptService.getPromptVersions(any(), any())).thenReturn(model);

        var body = new PromptVersionsRequestDto();
        body.setFolderId(folderId);
        body.setName(name);
        var dtoJson = ResourceUtils.readResource("/prompts/prompt_versions_info_dto.json");
        mockMvc.perform(post("/api/v1/prompts/versions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));

        verify(promptService).getPromptVersions(eq(folderId), eq(name));
    }

    @Test
    void testCreatePrompt() throws Exception {
        var createPromptDtoJson = ResourceUtils.readResource("/prompts/create_prompt_dto.json");

        var createPromptJson = ResourceUtils.readResource("/prompts/create_prompt.json");
        var createPrompt = objectMapper.readValue(createPromptJson, new TypeReference<CreatePrompt>() {
        });

        var createdPromptJson = ResourceUtils.readResource("/prompts/created_prompt.json");
        var createdPrompt = objectMapper.readValue(createdPromptJson, new TypeReference<Prompt>() {
        });

        var createdPromptDtoJson = ResourceUtils.readResource("/prompts/created_prompt_dto.json");

        when(promptService.createPrompt(any(), anyBoolean(), any())).thenReturn(createdPrompt);

        mockMvc.perform(post("/api/v1/prompts/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPromptDtoJson))
                .andExpect(status().isOk())
                .andExpect(content().json(createdPromptDtoJson, JsonCompareMode.LENIENT));

        verify(promptService).createPrompt(eq(createPrompt), eq(true), isNull());
    }

    @Test
    void testDeletePrompt() throws Exception {
        var promptPath = "testPath/TestName";
        var body = new PromptPathDto();
        body.setPath(promptPath);
        mockMvc.perform(post("/api/v1/prompts/delete")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(promptService).deletePrompt(promptPath);
    }

    @Test
    void testDeletePrompts() throws Exception {
        var promptPath1 = "testPath1/TestName";
        var promptPath2 = "testPath2/TestName";

        var promptPathDto1 = new PromptPathDto();
        promptPathDto1.setPath(promptPath1);

        var promptPathDto2 = new PromptPathDto();
        promptPathDto2.setPath(promptPath2);

        var promptPathsDto = new PromptPathsDto();
        promptPathsDto.setPaths(List.of(promptPathDto1, promptPathDto2));

        mockMvc.perform(post("/api/v1/prompts/delete/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(promptPathsDto)))
                .andExpect(status().isOk());

        verify(promptService).deletePrompts(List.of(promptPath1, promptPath2));
    }

    @Test
    void testDeletePrompts_NullPaths_ThrowValidationError() throws Exception {
        var promptPathsDto = new PromptPathsDto();

        mockMvc.perform(post("/api/v1/prompts/delete/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(promptPathsDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("paths: must not be empty"));

        verifyNoInteractions(promptService);
    }

    @Test
    void testDeletePrompts_EmptyPaths_ThrowValidationError() throws Exception {
        var promptPathsDto = new PromptPathsDto();

        mockMvc.perform(post("/api/v1/prompts/delete/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(promptPathsDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("paths: must not be empty"));

        verifyNoInteractions(promptService);
    }

    @Test
    void testDeletePrompts_PathsContainNullPath_ThrowValidationError() throws Exception {
        var promptPathDto1 = new PromptPathDto();
        promptPathDto1.setPath("testPath1/TestName");

        var promptPathDto2 = new PromptPathDto();
        promptPathDto2.setPath(null);

        var promptPathsDto = new PromptPathsDto();
        promptPathsDto.setPaths(List.of(promptPathDto1, promptPathDto2));

        mockMvc.perform(post("/api/v1/prompts/delete/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(promptPathsDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("JSON parse error: path: must not be empty"));

        verifyNoInteractions(promptService);
    }

    @Test
    void testMovePrompt() throws Exception {
        var movePromptDtoJson = ResourceUtils.readResource("/prompts/move_prompt_dto.json");

        var movePromptJson = ResourceUtils.readResource("/prompts/move_prompt.json");
        var movePrompt = objectMapper.readValue(movePromptJson, new TypeReference<MoveResource>() {
        });

        mockMvc.perform(post("/api/v1/prompts/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(movePromptDtoJson))
                .andExpect(status().isOk());

        verify(promptService).movePrompt(movePrompt);
    }

    @Test
    void testExportPromptsToZip() throws Exception {
        var paths = List.of("public/path1", "public/path2");

        var data = "some data";
        StreamingResponseBody streamingResponse = outputStream -> {
            outputStream.write(data.getBytes());
        };
        when(zipPromptEximService.exportPrompts(paths)).thenReturn(streamingResponse);

        var body = new ExportDto();
        body.setPaths(paths);
        mockMvc.perform(post("/api/v1/prompts/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"prompts_export.zip\""));
    }

    @Test
    void testExportPromptsToZip_PathNotStartsWithPublic_ThrowValidationError() throws Exception {
        var paths = List.of("test/path1");

        var body = new ExportDto();
        body.setPaths(paths);
        mockMvc.perform(post("/api/v1/prompts/export")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("paths[0].<list element>: Path must start with 'public/'"));

        verifyNoInteractions(promptEximService);
    }

    @Test
    void testExportPromptsToJson() throws Exception {
        var paths = List.of("public/path1", "public/path2");

        var promptsEximJson = ResourceUtils.readResource("/prompts/prompts_exim.json");
        var promptsExim = objectMapper.readValue(promptsEximJson, new TypeReference<PromptsExim>() {
        });
        when(promptEximService.exportPrompts(paths)).thenReturn(promptsExim);

        var body = new ExportDto();
        body.setPaths(paths);
        var dtoJson = ResourceUtils.readResource("/prompts/prompts_exim_dto.json");
        mockMvc.perform(post("/api/v1/prompts/export/json")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testImportPromptsFromZip() throws Exception {
        var dtoJson = ResourceUtils.readResource("/prompts/import_zip_prompt_result_dto.json");

        var path = "public/";
        var configDto = new ImportResourcesDto();
        configDto.setPath(path);
        configDto.setConflictResolutionStrategy(ImportResourcesConflictResolutionStrategyDto.OVERRIDE);
        var config = new ImportResources();
        config.setPath(path);
        config.setConflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE);


        var configFile = new MockMultipartFile(
                "config",
                "config.json",
                MimeTypeUtils.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(configDto)
        );
        var zipFile = new MockMultipartFile(
                "file",
                "file.zip",
                MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE,
                "mockData".getBytes()
        );

        var importResult = ImportResourcesFileResult.builder()
                .importResults(List.of(
                        ImportResourcesResult.createSuccess("public/Name__1.0.0", "public/target/Name__1.0.0")
                ))
                .build();

        when(zipPromptEximService.importPrompts(config, zipFile)).thenReturn(importResult);

        mockMvc.perform(multipart("/api/v1/prompts/import/zip")
                        .file(configFile)
                        .file(zipFile))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testImportPromptsFromZip_PathNotStartsWithPublic_ThrowValidationError() throws Exception {
        var path = "test/";
        var configDto = new ImportResourcesDto();
        configDto.setPath(path);
        configDto.setConflictResolutionStrategy(ImportResourcesConflictResolutionStrategyDto.OVERRIDE);

        var configFile = new MockMultipartFile(
                "config",
                "config.json",
                MimeTypeUtils.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(configDto)
        );
        var zipFile = new MockMultipartFile(
                "file",
                "file.zip",
                MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE,
                "mockData".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/prompts/import/zip")
                        .file(configFile)
                        .file(zipFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("path: Path must start with 'public/'"));

        verifyNoInteractions(promptEximService);
    }

    @Test
    void testImportPromptsFromZip_PathSegmentEndsWithDot_ThrowValidationError() throws Exception {
        var path = "public/../";
        var configDto = new ImportResourcesDto();
        configDto.setPath(path);
        configDto.setConflictResolutionStrategy(ImportResourcesConflictResolutionStrategyDto.OVERRIDE);

        var configFile = new MockMultipartFile(
                "config",
                "config.json",
                MimeTypeUtils.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(configDto)
        );
        var zipFile = new MockMultipartFile(
                "file",
                "file.zip",
                MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE,
                "mockData".getBytes()
        );

        mockMvc.perform(multipart("/api/v1/prompts/import/zip")
                        .file(configFile)
                        .file(zipFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("path: Resource name and/or parent folders must not end with .(dot)"));

        verifyNoInteractions(promptEximService);
    }

    @Test
    void testPreviewImportPromptsFromZip() throws Exception {
        var dtoJson = ResourceUtils.readResource("/prompts/import_preview_zip_prompt_result_dto.json");

        var path = "public/";

        var configDto = new ImportResourcesDto();
        configDto.setPath(path);
        configDto.setConflictResolutionStrategy(ImportResourcesConflictResolutionStrategyDto.OVERRIDE);

        var config = new ImportResources();
        config.setPath(path);
        config.setConflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE);


        var configFile = new MockMultipartFile(
                "config",
                "config.json",
                MimeTypeUtils.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(configDto)
        );
        var zipFile = new MockMultipartFile(
                "file",
                "file.zip",
                MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE,
                "mockData".getBytes()
        );

        var importResourcesPreview = ImportResourcesPreview.builder()
                .resourcePreviews(List.of(
                        ImportResourcePreview.builder()
                                .name("name")
                                .version("1.0.0")
                                .fileName("file1.json")
                                .build()
                ))
                .build();

        when(zipPromptEximService.previewImportPromptsFromZip(config, zipFile))
                .thenReturn(importResourcesPreview);

        mockMvc.perform(multipart("/api/v1/prompts/import/zip/preview")
                        .file(configFile)
                        .file(zipFile))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testImportPromptsFromJson() throws Exception {
        var requestDtoJson = ResourceUtils.readResource("/prompts/import_json_prompt_request_dto.json");
        var requestDto = objectMapper.readValue(requestDtoJson, new TypeReference<PromptsEximDto>() {
        });

        var responseDtoJson = ResourceUtils.readResource("/prompts/import_json_prompt_response_dto.json");

        var path = "public/";
        var configDto = new ImportResourcesDto();
        configDto.setPath(path);
        configDto.setConflictResolutionStrategy(ImportResourcesConflictResolutionStrategyDto.OVERRIDE);
        var config = new ImportResources();
        config.setPath(path);
        config.setConflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE);

        var configFile = new MockMultipartFile(
                "config",
                "config.json",
                MimeTypeUtils.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(configDto)
        );
        var jsonFile = new MockMultipartFile(
                "file",
                "file.json",
                MimeTypeUtils.APPLICATION_JSON_VALUE,
                requestDtoJson.getBytes()
        );

        var importResult = ImportResourcesFileResult.builder()
                .importResults(List.of(
                        ImportResourcesResult.createSuccess("public/Name__1.0.0", "public/target/Name__1.0.0")
                ))
                .build();

        when(promptEximService.importPrompts(config, requestDto)).thenReturn(importResult);

        mockMvc.perform(multipart("/api/v1/prompts/import/json")
                        .file(configFile)
                        .file(jsonFile))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
                .andExpect(content().json(responseDtoJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testImportPromptsFromJson_PathNotStartsWithPublic_ThrowValidationError() throws Exception {
        var requestDtoJson = ResourceUtils.readResource("/prompts/import_json_prompt_request_dto.json");

        var path = "test/";
        var configDto = new ImportResourcesDto();
        configDto.setPath(path);
        configDto.setConflictResolutionStrategy(ImportResourcesConflictResolutionStrategyDto.OVERRIDE);

        var configFile = new MockMultipartFile(
                "config",
                "config.json",
                MimeTypeUtils.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(configDto)
        );
        var jsonFile = new MockMultipartFile(
                "file",
                "file.json",
                MimeTypeUtils.APPLICATION_JSON_VALUE,
                requestDtoJson.getBytes()
        );

        mockMvc.perform(multipart("/api/v1/prompts/import/json")
                        .file(configFile)
                        .file(jsonFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("path: Path must start with 'public/'"));

        verifyNoInteractions(promptEximService);
    }

    @Test
    void testImportPromptsFromJson_ConfigContainsPathSegmentThatEndsWithDot_ThrowValidationError() throws Exception {
        var requestDtoJson = ResourceUtils.readResource("/prompts/import_json_prompt_request_dto.json");

        var path = "public/../";
        var configDto = new ImportResourcesDto();
        configDto.setPath(path);
        configDto.setConflictResolutionStrategy(ImportResourcesConflictResolutionStrategyDto.OVERRIDE);

        var configFile = new MockMultipartFile(
                "config",
                "config.json",
                MimeTypeUtils.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(configDto)
        );
        var jsonFile = new MockMultipartFile(
                "file",
                "file.json",
                MimeTypeUtils.APPLICATION_JSON_VALUE,
                requestDtoJson.getBytes()
        );

        mockMvc.perform(multipart("/api/v1/prompts/import/json")
                        .file(configFile)
                        .file(jsonFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("path: Resource name and/or parent folders must not end with .(dot)"));

        verifyNoInteractions(promptEximService);
    }

    @Test
    void testImportPromptsFromJson_FileContainsPathSegmentThatEndsWithDot_ThrowValidationError() throws Exception {
        var requestDtoJson = ResourceUtils.readResource("/prompts/import_json_prompt_request_traversal_dto.json");

        var path = "public/";
        var configDto = new ImportResourcesDto();
        configDto.setPath(path);
        configDto.setConflictResolutionStrategy(ImportResourcesConflictResolutionStrategyDto.OVERRIDE);

        var configFile = new MockMultipartFile(
                "config",
                "config.json",
                MimeTypeUtils.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(configDto)
        );
        var jsonFile = new MockMultipartFile(
                "file",
                "file.json",
                MimeTypeUtils.APPLICATION_JSON_VALUE,
                requestDtoJson.getBytes()
        );

        mockMvc.perform(multipart("/api/v1/prompts/import/json")
                        .file(configFile)
                        .file(jsonFile))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("JSON parse error: id: Resource name and/or parent folders must not end with .(dot)"));

        verifyNoInteractions(promptEximService);
    }

}
