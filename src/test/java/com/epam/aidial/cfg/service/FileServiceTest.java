package com.epam.aidial.cfg.service;

import com.epam.aidial.cfg.client.FileClient;
import com.epam.aidial.cfg.client.ResourceClient;
import com.epam.aidial.cfg.client.dto.FileMetadataDto;
import com.epam.aidial.cfg.client.dto.MoveResourceDto;
import com.epam.aidial.cfg.client.dto.NodeTypeDto;
import com.epam.aidial.cfg.client.mapper.FileClientMapperImpl;
import com.epam.aidial.cfg.client.mapper.FolderMapperImpl;
import com.epam.aidial.cfg.client.mapper.ResourceClientMapperImpl;
import com.epam.aidial.cfg.model.ExportResource;
import com.epam.aidial.cfg.model.FileNodeInfo;
import com.epam.aidial.cfg.model.FolderInfo;
import com.epam.aidial.cfg.model.ImportConflictResolutionStrategy;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ImportResourcesFileResult;
import com.epam.aidial.cfg.model.MoveResource;
import com.epam.aidial.cfg.model.NodeType;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import feign.Response;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {
        ResourceClientMapperImpl.class,
        FileClientMapperImpl.class,
        FolderMapperImpl.class,
        ResourceImportValidator.class,
        FileService.class
})
@TestPropertySource(properties = {
        "files.import.consecutiveErrorsThreshold=2"
})
class FileServiceTest {

    @MockitoBean
    private FileClient fileClient;

    @MockitoBean
    private ResourceClient resourceClient;

    @Autowired
    private FileService fileService;

    @Autowired
    private ResourceImportValidator resourceImportValidator;

    @Test
    void testGetAll() {
        // given
        ResourceMetadataRequest filesRequest = new ResourceMetadataRequest();
        FileMetadataDto fileMetadataDto = new FileMetadataDto();
        fileMetadataDto.setNodeType(NodeTypeDto.ITEM);
        fileMetadataDto.setUrl("files/public/testFile.txt");
        fileMetadataDto.setName("testFile.txt");
        FileNodeInfo expected = new FileNodeInfo();
        expected.setName("testFile.txt");
        expected.setPath("public/testFile.txt");
        expected.setFolderId("public");
        expected.setNodeType(NodeType.ITEM);
        when(fileClient.getFilesMetadata(any(), anyBoolean(), any(), anyBoolean())).thenReturn(fileMetadataDto);
        // when
        FileNodeInfo result = fileService.getAll(filesRequest);
        // then
        Assertions.assertThat(result).isNotNull();
        Assertions.assertThat(result).isEqualTo(expected);
        verify(fileClient).getFilesMetadata(any(), anyBoolean(), any(), anyBoolean());
    }

    @Test
    void testGet() {
        // given
        String path = "public/test.txt";
        Response response = mock(Response.class);
        when(fileClient.getFile(path)).thenReturn(response);
        // when
        Response result = fileService.get(path);
        // then
        Assertions.assertThat(result).isEqualTo(response);
    }

    @Test
    void testUploadFile() {
        // given
        MultipartFile multipart = mock(MultipartFile.class);
        String path = "public/";
        String expectedFileName = path + multipart.getOriginalFilename();
        ImportResources importResources = ImportResources.builder()
                .path(path)
                .conflictResolutionStrategy(ImportConflictResolutionStrategy.OVERRIDE)
                .build();
        // when
        ImportResourcesFileResult result = fileService.uploadFile(List.of(multipart), importResources);
        // then
        Assertions.assertThat(result).isNotNull();
        verify(fileClient).uploadFile(multipart, expectedFileName, Map.of("If-Match", "*"));
    }

    @Test
    void testDeleteFile() {
        // given
        String path = "public/test.txt";
        // when
        fileService.deleteFile(path);
        // then
        verify(fileClient).deleteFile(path);
    }

    @Test
    void testMove() {
        // given
        MoveResource moveResource = MoveResource.builder()
                .sourceUrl("public/test.txt")
                .destinationUrl("public/test/test.txt")
                .build();
        ArgumentCaptor<MoveResourceDto> argumentCaptor = ArgumentCaptor.forClass(MoveResourceDto.class);
        // when
        fileService.move(moveResource);
        // then
        verify(resourceClient).move(argumentCaptor.capture());
        Assertions.assertThat(argumentCaptor.getValue()).satisfies(dto -> {
            Assertions.assertThat(dto).isNotNull();
            Assertions.assertThat(dto.getSourceUrl()).isEqualTo("files/public/test.txt");
            Assertions.assertThat(dto.getDestinationUrl()).isEqualTo("files/public/test/test.txt");
            Assertions.assertThat(dto.isOverwrite()).isFalse();
        });
    }

    @Test
    void testGetFolders() {
        // given
        ResourceMetadataRequest request = ResourceMetadataRequest.builder()
                .path("public/")
                .build();
        FileMetadataDto item = FileMetadataDto.builder()
                .nodeType(NodeTypeDto.ITEM)
                .url("files/public/testFile.txt")
                .name("testFile.txt")
                .build();
        FileMetadataDto folder = FileMetadataDto.builder()
                .nodeType(NodeTypeDto.FOLDER)
                .url("files/public/test/")
                .name("test")
                .build();
        FileMetadataDto response = FileMetadataDto.builder()
                .nodeType(NodeTypeDto.FOLDER)
                .url("public/")
                .bucket("public")
                .items(List.of(item, folder))
                .build();
        when(fileClient.getFilesMetadata(any(), anyBoolean(), any(), anyBoolean())).thenReturn(response);
        // when
        FolderInfo folderInfo = fileService.getFolders(request);
        // then
        Assertions.assertThat(folderInfo).isNotNull().satisfies(info ->
                Assertions.assertThat(info.getItems()).hasSize(1));
    }

    @Test
    void testGetItemResources() {
        // given
        String path = "public/test/";
        FileMetadataDto item1 = FileMetadataDto.builder()
                .nodeType(NodeTypeDto.ITEM)
                .url("files/public/test/test2/testFile2.txt")
                .name("testFile.txt")
                .build();
        FileMetadataDto folder = FileMetadataDto.builder()
                .nodeType(NodeTypeDto.FOLDER)
                .url("files/public/test/test2/")
                .name("test2")
                .items(List.of(item1))
                .build();
        FileMetadataDto item2 = FileMetadataDto.builder()
                .nodeType(NodeTypeDto.ITEM)
                .url("files/public/test/testFile.txt")
                .name("testFile.txt")
                .build();
        FileMetadataDto response = FileMetadataDto.builder()
                .nodeType(NodeTypeDto.FOLDER)
                .url("public/test/")
                .bucket("public")
                .items(List.of(item2, folder))
                .build();
        when(fileClient.getFilesMetadata(any(), anyBoolean(), any(), anyBoolean())).thenReturn(response);
        // when
        Set<String> resourceUrls = fileService.getResourceUrls(path);
        // then
        Assertions.assertThat(resourceUrls)
                .containsExactlyInAnyOrder("files/public/test/testFile.txt",
                        "files/public/test/test2/testFile2.txt");
    }

    @Test
    void testExport_whenPathsAreFiles() throws Exception {
        // given
        ExportResource exportResource = new ExportResource();
        exportResource.setPaths(List.of("public/test/test1.txt", "public/test2.txt"));
        Response mockResponse = mock(Response.class);
        Response.Body body = mock(Response.Body.class);

        when(fileClient.getFile(anyString())).thenReturn(mockResponse);
        when(mockResponse.body()).thenReturn(body);
        when(body.asInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));

        // when
        StreamingResponseBody stream = fileService.export(exportResource);
        stream.writeTo(new ByteArrayOutputStream());

        // then
        verify(fileClient, never()).getFilesMetadata(any(), anyBoolean(), any(), anyBoolean());
        verify(fileClient).getFile("public/test/test1.txt");
        verify(fileClient).getFile("public/test2.txt");
    }

    @Test
    void testExport_whenPathIsFolder() throws Exception {
        // given
        ExportResource exportResource = new ExportResource();
        exportResource.setPaths(List.of("public/test/"));
        FileMetadataDto item1 = FileMetadataDto.builder()
                .nodeType(NodeTypeDto.ITEM)
                .url("files/public/test/test1.txt")
                .name("test1.txt")
                .build();
        FileMetadataDto item2 = FileMetadataDto.builder()
                .nodeType(NodeTypeDto.ITEM)
                .url("files/public/test/test2.txt")
                .name("test2.txt")
                .build();
        FileMetadataDto folderResponse = FileMetadataDto.builder()
                .nodeType(NodeTypeDto.FOLDER)
                .url("files/public/test/")
                .items(List.of(item1, item2))
                .build();
        when(fileClient.getFilesMetadata(eq("public/test/"), eq(true), any(), anyBoolean())).thenReturn(folderResponse);
        Response mockResponse = mock(Response.class);
        Response.Body body = mock(Response.Body.class);
        when(fileClient.getFile(anyString())).thenReturn(mockResponse);
        when(mockResponse.body()).thenReturn(body);
        when(body.asInputStream()).thenReturn(new ByteArrayInputStream("content".getBytes()));

        // when
        StreamingResponseBody stream = fileService.export(exportResource);
        stream.writeTo(new ByteArrayOutputStream());

        // then
        verify(fileClient).getFilesMetadata(eq("public/test/"), eq(true), any(), anyBoolean());
        verify(fileClient).getFile("public/test/test1.txt");
        verify(fileClient).getFile("public/test/test2.txt");
    }

}