package com.epam.aidial.cfg.web.controller.none;

import com.epam.aidial.cfg.configuration.JsonMapperConfiguration;
import com.epam.aidial.cfg.dto.FileNodeInfoDto;
import com.epam.aidial.cfg.dto.FilePathDto;
import com.epam.aidial.cfg.dto.FilePathsDto;
import com.epam.aidial.cfg.dto.ImportResourcesConflictResolutionStrategyDto;
import com.epam.aidial.cfg.dto.ImportResourcesDto;
import com.epam.aidial.cfg.dto.ImportResourcesFileResultDto;
import com.epam.aidial.cfg.dto.MoveResourceDto;
import com.epam.aidial.cfg.mapper.FileMapperImpl;
import com.epam.aidial.cfg.mapper.ResourceMapperImpl;
import com.epam.aidial.cfg.utils.ResourceUtils;
import com.epam.aidial.cfg.web.controller.FileController;
import com.epam.aidial.cfg.web.facade.FileResourceFacade;
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
    private FileResourceFacade facade;

    @Test
    void testGetAll() throws Exception {
        // given
        String filesInfoRequestDtoJson = ResourceUtils.readResource("/files/files_infos_request_dto.json");
        String modelJson = ResourceUtils.readResource("/files/file_infos.json");
        FileNodeInfoDto model = objectMapper.readValue(modelJson, new TypeReference<>() {
        });
        when(facade.getAll(any())).thenReturn(model);
        String dtoJson = ResourceUtils.readResource("/files/file_infos_dto.json");

        // when
        mockMvc.perform(post("/api/v1/files")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(filesInfoRequestDtoJson))
                // then
                .andExpect(status().isOk())
                .andExpect(content().json(dtoJson, JsonCompareMode.LENIENT));
    }

    @Test
    void testUploadFile() throws Exception {
        // given
        MockMultipartFile mockFile = new MockMultipartFile("files", "test.txt", MediaType.TEXT_PLAIN_VALUE, "test content".getBytes());
        String path = "public/uploads/test.txt";
        ImportResourcesDto importResources = new ImportResourcesDto();
        importResources.setPath(path);
        importResources.setConflictResolutionStrategy(ImportResourcesConflictResolutionStrategyDto.OVERRIDE);
        ImportResourcesFileResultDto importResult = new ImportResourcesFileResultDto();
        when(facade.uploadFiles(List.of(mockFile), importResources)).thenReturn(importResult);

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
        verify(facade).uploadFiles(List.of(mockFile), importResources);
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
        ImportResourcesDto importResources = new ImportResourcesDto();
        importResources.setPath(path);
        importResources.setConflictResolutionStrategy(ImportResourcesConflictResolutionStrategyDto.OVERRIDE);
        ImportResourcesFileResultDto importResult = new ImportResourcesFileResultDto();
        when(facade.uploadFilesZip(zipFile, importResources)).thenReturn(importResult);

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
    }

    @Test
    void testDownload() throws Exception {
        // given
        String path = "public/to/file.txt";
        String fileName = "file.txt";
        byte[] fileContent = "This is the content of the file".getBytes(StandardCharsets.UTF_8);

        Response mockResponse = mock(Response.class);
        Response.Body responceBody = mock(Response.Body.class);

        when(facade.getByPath(path)).thenReturn(mockResponse);
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

        verify(facade).getByPath(path);
    }

    @Test
    void testDownload_fileNotFound() throws Exception {
        // given
        String path = "public/to/nonexistent.txt";
        Response mockResponse = mock(Response.class);
        when(facade.getByPath(path)).thenReturn(mockResponse);
        when(mockResponse.status()).thenReturn(404);

        // when
        mockMvc.perform(get("/api/v1/files/download")
                        .param("path", path))
                // then
                .andExpect(status().isNotFound());

        verify(facade).getByPath(path);
    }

    @Test
    void testDeleteFile() throws Exception {
        // given
        String path = "public/to/file.txt";
        doNothing().when(facade).deleteFile(path);

        // when
        mockMvc.perform(delete("/api/v1/files")
                        .param("path", path))
                // then
                .andExpect(status().isOk());
        verify(facade).deleteFile(path);
    }

    @Test
    void testDeleteFiles() throws Exception {
        // given
        var path1 = "public/testPath1/TestName";
        var path2 = "public/testPath2/TestName";

        var pathDto1 = new FilePathDto();
        pathDto1.setPath(path1);

        var pathDto2 = new FilePathDto();
        pathDto2.setPath(path2);

        var pathsDto = new FilePathsDto();
        pathsDto.setPaths(List.of(pathDto1, pathDto2));
        doNothing().when(facade).deleteFiles(pathsDto);

        // when
        mockMvc.perform(post("/api/v1/files/delete/bulk")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pathsDto)))
                // then
                .andExpect(status().isOk());
        verify(facade).deleteFiles(pathsDto);
    }

    @Test
    void testMoveFile() throws Exception {
        var moveFileDtoJson = ResourceUtils.readResource("/files/move_file_dto.json");

        var moveFileJson = ResourceUtils.readResource("/files/move_file.json");
        var moveFile = objectMapper.readValue(moveFileJson, new TypeReference<MoveResourceDto>() {
        });

        mockMvc.perform(post("/api/v1/files/move")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(moveFileDtoJson))
                .andExpect(status().isOk());

        verify(facade).moveFile(moveFile);
    }

}