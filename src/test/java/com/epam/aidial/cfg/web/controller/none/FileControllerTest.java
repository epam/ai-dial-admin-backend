package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.ImportResourcesConflictResolutionStrategyDto;
import com.epam.aidial.cfg.dto.ImportResourcesDto;
import com.epam.aidial.cfg.mapper.FileMapperImpl;
import com.epam.aidial.cfg.mapper.ResourceMapperImpl;
import com.epam.aidial.cfg.model.FileNodeInfo;
import com.epam.aidial.cfg.model.ImportConflictResolutionStrategy;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ImportResourcesFileResult;
import com.epam.aidial.cfg.model.MoveResource;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.service.FileService;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.FileController;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.Response;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.json.JsonCompareMode;
import org.springframework.util.MimeTypeUtils;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = FileController.class)
@Import({
        JsonMapperConfiguration.class,
        FileMapperImpl.class,
        ResourceMapperImpl.class
})
class FileControllerTest extends AbstractControllerNoneSecureTest {

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private FileService fileService;

    @Test
    void testGetAll() throws Exception {
        // given
        String filesInfoRequestDtoJson = ResourceUtils.readResource("/files/files_infos_request_dto.json");
        String filesInfoRequestJson = ResourceUtils.readResource("/files/file_infos_request.json");
        ResourceMetadataRequest filesInfoRequest = objectMapper.readValue(filesInfoRequestJson, new TypeReference<>() {
        });
        String modelJson = ResourceUtils.readResource("/files/file_infos.json");
        FileNodeInfo model = objectMapper.readValue(modelJson, new TypeReference<>() {
        });
        when(fileService.getAll(any())).thenReturn(model);
        String dtoJson = ResourceUtils.readResource("/files/file_infos_dto.json");

        // when
        mockMvc.perform(post("/api/v1/files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filesInfoRequestDtoJson))
                // then
                .andExpect(status().isOk())
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));

        verify(fileService).getAll(eq(filesInfoRequest));
    }

    @Test
    void testUploadFile() throws Exception {
        // given
        MockMultipartFile mockFile = new MockMultipartFile("files", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());
        String path = "public/uploads/test.txt";
        ImportResources importResources = ImportResources.builder()
                .path(path)
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();
        ImportResourcesFileResult importResult = ImportResourcesFileResult.builder()
                .build();
        when(fileService.uploadFile(List.of(mockFile), importResources)).thenReturn(importResult);

        var configDto = new ImportResourcesDto();
        configDto.setPath(path);
        configDto.setConflictResolutionStrategy(ImportResourcesConflictResolutionStrategyDto.OVERRIDE);
        MockMultipartFile configFile = new MockMultipartFile("config", "config.json", MimeTypeUtils.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(configDto));

        // when
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/files/import")
                        .file(mockFile)
                        .file(configFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                // then
                .andExpect(status().isOk());
        verify(fileService).uploadFile(List.of(mockFile), importResources);
    }

    @Test
    void testUploadZipFile() throws Exception {
        // given
        var zipFile = new MockMultipartFile(
                "file",
                "file.zip",
                MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE,
                "mockData".getBytes()
        );
        String path = "public/uploads/";
        ImportResources importResources = ImportResources.builder()
                .path(path)
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();
        ImportResourcesFileResult importResult = ImportResourcesFileResult.builder()
                .build();
        when(fileService.uploadFileZip(importResources, zipFile)).thenReturn(importResult);

        var configDto = new ImportResourcesDto();
        configDto.setPath(path);
        configDto.setConflictResolutionStrategy(ImportResourcesConflictResolutionStrategyDto.OVERRIDE);
        MockMultipartFile configFile = new MockMultipartFile("config", "config.json", MimeTypeUtils.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(configDto));

        // when
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/files/import/zip")
                        .file(zipFile)
                        .file(configFile)
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                // then
                .andExpect(status().isOk());
        verify(fileService).uploadFileZip(importResources, zipFile);
    }

    @Test
    void testDownload() throws Exception {
        // given
        String path = "public/to/file.txt";
        String fileName = "file.txt";
        byte[] fileContent = "This is the content of the file".getBytes(StandardCharsets.UTF_8);

        Response mockResponse = mock(Response.class);
        Response.Body responceBody = mock(Response.Body.class);

        when(fileService.get(path)).thenReturn(mockResponse);
        when(mockResponse.status()).thenReturn(200);
        when(mockResponse.body()).thenReturn(responceBody);
        when(responceBody.asInputStream()).thenReturn(new ByteArrayInputStream(fileContent));

        // when
        mockMvc.perform(get("/api/v1/files/download")
                        .param("path", path))
                // then
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + fileName + "\""))
                .andExpect(result -> {
                    byte[] actualContent = result.getResponse().getContentAsByteArray();
                    Assertions.assertThat(actualContent).isEqualTo(fileContent);
                });

        verify(fileService).get(path);
    }

    @Test
    void testDownload_fileNotFound() throws Exception {
        // given
        String path = "public/to/nonexistent.txt";
        Response mockResponse = mock(Response.class);
        when(fileService.get(path)).thenReturn(mockResponse);
        when(mockResponse.status()).thenReturn(404);

        // when
        mockMvc.perform(get("/api/v1/files/download")
                        .param("path", path))
                // then
                .andExpect(status().isNotFound());

        verify(fileService).get(path);
    }

    @Test
    void testDeleteFile() throws Exception {
        // given
        String path = "public/to/file.txt";
        doNothing().when(fileService).deleteFile(path);

        // when
        mockMvc.perform(delete("/api/v1/files")
                        .param("path", path))
                // then
                .andExpect(status().isOk());
        verify(fileService).deleteFile(path);
    }

    @Test
    void testMoveFile() throws Exception {
        var moveFileDtoJson = ResourceUtils.readResource("/files/move_file_dto.json");

        var moveFileJson = ResourceUtils.readResource("/files/move_file.json");
        var moveFile = objectMapper.readValue(moveFileJson, new TypeReference<MoveResource>() {
        });

        mockMvc.perform(post("/api/v1/files/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(moveFileDtoJson))
                .andExpect(status().isOk());

        verify(fileService).move(moveFile);
    }

}