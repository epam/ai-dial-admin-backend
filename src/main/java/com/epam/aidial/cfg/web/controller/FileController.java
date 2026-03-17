package com.epam.aidial.cfg.web.controller;

import com.epam.aidial.cfg.dto.ExportDto;
import com.epam.aidial.cfg.dto.FileNodeInfoDto;
import com.epam.aidial.cfg.dto.FilePathDto;
import com.epam.aidial.cfg.dto.FilePathsDto;
import com.epam.aidial.cfg.dto.ImportResourcesDto;
import com.epam.aidial.cfg.dto.ImportResourcesFileResultDto;
import com.epam.aidial.cfg.dto.MoveResourceDto;
import com.epam.aidial.cfg.dto.ResourceMetadataRequestDto;
import com.epam.aidial.cfg.dto.validation.annotation.MetadataPath;
import com.epam.aidial.cfg.mapper.FileMapper;
import com.epam.aidial.cfg.mapper.ResourceMapper;
import com.epam.aidial.cfg.model.ImportResources;
import com.epam.aidial.cfg.model.ResourceMetadataRequest;
import com.epam.aidial.cfg.model.UpdateRulesRequest;
import com.epam.aidial.cfg.service.FileService;
import com.epam.aidial.cfg.service.FolderService;
import feign.Response;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.MimeTypeUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/api/v1/files")
@Validated
@RequiredArgsConstructor
public class FileController {

    private final FileService fileService;
    private final FolderService folderService;
    private final FileMapper fileMapper;
    private final ResourceMapper resourceMapper;

    @PostMapping()
    public FileNodeInfoDto getAll(@RequestBody(required = false) ResourceMetadataRequestDto requestDto) {
        ResourceMetadataRequest request = resourceMapper.toRequest(requestDto);
        var filesInfo = fileService.getAll(request);
        return fileMapper.toFilesInfoDto(filesInfo);
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> download(@RequestParam("path") String path) throws IOException {
        Response response = fileService.get(path);
        int status = response.status();
        if (status == 200) {
            String fileName = extractFileName(path);
            HttpHeaders httpHeaders = new HttpHeaders();
            String contentType = extractContentType(response);
            if (StringUtils.isNotEmpty(contentType)) {
                httpHeaders.set(HttpHeaders.CONTENT_TYPE, contentType);
            }
            httpHeaders.set(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=\"" + fileName + "\"");
            return ResponseEntity.ok()
                    .headers(httpHeaders)
                    .body(response.body().asInputStream().readAllBytes());
        }
        return ResponseEntity.status(HttpStatusCode.valueOf(status)).build();
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResourcesFileResultDto uploadFiles(@RequestPart("files") @Valid @Size(min = 1, max = 30) List<MultipartFile> files,
                                                    @RequestPart("config") @Validated ImportResourcesDto importFilesDto) {
        var importFiles = resourceMapper.toImportResources(importFilesDto);
        importFolderRules(importFiles);
        var importResults = fileService.uploadFile(files, importFiles);
        return resourceMapper.toImportResourcesFileResultDto(importResults);
    }

    @PostMapping(value = "/import/zip", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ImportResourcesFileResultDto uploadFilesZip(@RequestPart("file") MultipartFile files,
                                                       @RequestPart("config") @Validated ImportResourcesDto importFilesDto) {
        var importFiles = resourceMapper.toImportResources(importFilesDto);
        importFolderRules(importFiles);
        var importResult = fileService.uploadFileZip(importFiles, files);
        return resourceMapper.toImportResourcesFileResultDto(importResult);
    }

    @DeleteMapping()
    public void deleteFile(@RequestParam("path") @MetadataPath String path) {
        fileService.deleteFile(path);
    }

    @PostMapping(value = "/delete/bulk")
    public void deleteFiles(@RequestBody FilePathsDto filePaths) {
        var paths = filePaths.getPaths().stream().map(FilePathDto::getPath).toList();
        fileService.deleteFiles(paths);
    }

    @PostMapping(path = "/move",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public void moveFile(@RequestBody MoveResourceDto moveResourceDto) {
        var moveResource = resourceMapper.toMoveResource(moveResourceDto);
        fileService.move(moveResource);
    }

    @PostMapping(path = "/export",
            consumes = MimeTypeUtils.APPLICATION_JSON_VALUE)
    public ResponseEntity<StreamingResponseBody> exportPromptsToZip(@RequestBody ExportDto exportDto) {
        var exportResource = resourceMapper.toExportResource(exportDto);
        var stream = fileService.export(exportResource);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, MimeTypeUtils.APPLICATION_OCTET_STREAM_VALUE);
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"files_export.zip\"");
        return ResponseEntity.ok()
                .headers(headers)
                .body(stream);
    }

    private String extractContentType(Response response) {
        Collection<String> contentTypes = response.headers().get("Content-Type");

        if (CollectionUtils.isNotEmpty(contentTypes)) {
            String firstContentType = contentTypes.iterator().next();
            return MediaType.parseMediaType(firstContentType).toString();
        }
        return null;
    }

    private static String extractFileName(String path) {
        int lastSlashIndex = path.lastIndexOf('/');
        if (lastSlashIndex == -1) {
            throw new IllegalArgumentException("The file path does not contain a '/': %s".formatted(path));
        }
        return path.substring(lastSlashIndex + 1);
    }

    private void importFolderRules(ImportResources importFiles) {
        if (importFiles.getRules() != null) {
            var updateRulesRequest = UpdateRulesRequest.builder()
                    .targetFolder(importFiles.getPath())
                    .rules(importFiles.getRules())
                    .build();
            folderService.updatesRules(updateRulesRequest);
        }
    }
}